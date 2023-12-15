package org.wjx.service.Impl;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wjx.Exception.ClientException;
import org.wjx.Exception.ServiceException;
import org.wjx.Res;
import org.wjx.core.SafeCache;
import org.wjx.dao.DO.*;
import org.wjx.dao.mapper.*;
import org.wjx.dto.entiey.SeatClassDTO;
import org.wjx.dto.entiey.TicketListDTO;
import org.wjx.dto.req.CancelTicketOrderReqDTO;
import org.wjx.dto.req.PurchaseTicketReqDTO;
import org.wjx.dto.req.RefundTicketReqDTO;
import org.wjx.dto.req.TicketPageQueryReqDTO;
import org.wjx.dto.resp.*;
import org.wjx.enums.SeatStatusEnum;
import org.wjx.enums.SourceEnum;
import org.wjx.enums.TicketStatusEnum;
import org.wjx.filter.AbstractFilterChainsContext;
import org.wjx.handler.select.TrainSeatTypeSelector;
import org.wjx.remote.TicketOrderRemoteService;
import org.wjx.remote.dto.ResetSeatDTO;
import org.wjx.remote.dto.TicketOrderCreateRemoteReqDTO;
import org.wjx.remote.dto.TicketOrderItemCreateRemoteReqDTO;
import org.wjx.service.*;
import org.wjx.user.core.ApplicationContextHolder;
import org.wjx.user.core.UserContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.wjx.constant.RedisKeyConstant.*;
import static org.wjx.constant.SystemConstant.ADVANCE_TICKET_DAY;

/**
 * @author xiu
 * @create 2023-11-28 15:16
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TicketServiceImpl extends ServiceImpl<TicketMapper, TicketDO> implements TicketService {
    final AbstractFilterChainsContext chainsContext;
    final SafeCache cache;
    final RedissonClient redissonClient;
    final TrainStationRelationMapper trainStationRelationMapper;
    final TicketOrderRemoteService ticketOrderRemoteService;
    final TrainStationMapper trainStationMapper;
    final CarrageMapper carrageMapper;
    final TrainStationPriceMapper priceMapper;
    final SeatMapper seatMapper;
    final TrainSeatTypeSelector seatTypeSelector;
    final SeatService seatService;
    final TrainStationService trainStationService;
    final RegionService regionService;
    final TrainService trainService;


    /**
     * 根据条件分页查询车票
     * 大致流程:
     * <ol>
     *  <li>根据编号查出对应的城市名字</li>
     *  <li>查询train_station_relation表,查出发城市-目的城市的列车id,并且获得列车对应的出发站(不是起始站)和到达站(不是终点站)</li>
     *  <li>根据列车id列表查询carrage表,查出列车对应的座位类型</li>
     *  <li>根据列车id列表查询train_station,主要获得列车的线路(有顺序)</li>
     *  <li>根列车的线路生成所有可能的行程线路的起点和终点站的二维数组</li>
     *  <li>根据列车Id,二维数组座位type,三个for循环,遍历seat表查询座位的数量,保存缓存</li>
     *  <li>填充所有列车出现的车厢类型</li>
     *  <li>填充所有列车的起始站,和终点站</li>
     *  <li>遍历上边的结果train_station_price查询对应的价格和线路的历时并且生成缓存，按照历时升序排序后返回结果</li>
     * </ol>
     *
     * @param requestParam 分页查询车票请求参数
     * @return 查询车票返回结果
     */
    @Override
    @SneakyThrows
    public TicketPageQueryRespDTO pageListTicketQueryV1(TicketPageQueryReqDTO requestParam) {
        TicketPageQueryRespDTO ticketPageQueryRespDTO = gene(requestParam);
        List<TicketListDTO> gene = ticketPageQueryRespDTO.getTrainList();
        log.info("第一步查询的数据：{}",gene);
        ticketPageQueryRespDTO.setTrainList(gene);
        ticketPageQueryRespDTO.setDepartureStationList(gene.stream().map(TicketListDTO::getDeparture).collect(Collectors.toSet()).stream().toList());
        ticketPageQueryRespDTO.setArrivalStationList(gene.stream().map(TicketListDTO::getArrival).collect(Collectors.toSet()).stream().toList());
        setPriceAndTime(ticketPageQueryRespDTO);
        log.info("第2步查询的数据：{}",ticketPageQueryRespDTO);
        List<TicketListDTO> list = ticketPageQueryRespDTO.getTrainList().stream().sorted(Comparator.comparing(TicketListDTO::getDuration).reversed()).toList();
        ticketPageQueryRespDTO.setTrainList(list);
        return ticketPageQueryRespDTO;
    }

    /**
     * 给所有结果查出票价
     *
     * @param ticketPageQueryRespDTO
     */
    private void setPriceAndTime(TicketPageQueryRespDTO ticketPageQueryRespDTO) {
        List<TicketListDTO> trainList = ticketPageQueryRespDTO.getTrainList();
        for (TicketListDTO ticketListDTO : trainList) {
            String departure = ticketListDTO.getDeparture();
            String arrival = ticketListDTO.getArrival();
            String trainId = ticketListDTO.getTrainId();
            DateTime startDate = DateUtil.parse(ticketListDTO.getDepartureTime(), "yyyy-MM-dd HH:mm");
            DateTime endDate = DateUtil.parse(ticketListDTO.getArrivalTime(), "yyyy-MM-dd HH:mm");
            ticketListDTO.setDuration(DateUtil.formatBetween(startDate, endDate, BetweenFormatter.Level.HOUR));
            long days = DateUtil.between(startDate, endDate, DateUnit.DAY);
            ticketListDTO.setDaysArrived((int) days);
            for (SeatClassDTO seatClassDTO : ticketListDTO.getSeatClassList()) {
                Integer price = cache.SafeGetOfHash(TRAIN_PRICE_HASH +
                        String.join("-", trainId, departure, arrival), seatClassDTO.getType(), () -> {
                    TrainStationPriceDO trainStationPriceDO = priceMapper.selectOne(new LambdaQueryWrapper<TrainStationPriceDO>()
                            .eq(TrainStationPriceDO::getDeparture, departure)
                            .eq(TrainStationPriceDO::getArrival, arrival)
                            .eq(TrainStationPriceDO::getTrainId, trainId)
                            .eq(TrainStationPriceDO::getSeatType, seatClassDTO.getType())
                            .select(TrainStationPriceDO::getPrice));
                    return trainStationPriceDO.getPrice();
                });
                BigDecimal bigDecimal = new BigDecimal(price / 100).setScale(2, RoundingMode.HALF_UP);
                seatClassDTO.setPrice(bigDecimal);
            }
        }
    }

    /**
     * 生成 TicketPageQueryRespDTO的 List<TicketListDTO> 和SeatClassTypeList
     *
     * @param requestParam 请求参数
     * @return
     */
    private TicketPageQueryRespDTO gene(TicketPageQueryReqDTO requestParam) {
        String startregion = regionService.selectCacheRegionNameByCode(requestParam.getFromStation());
        String endregion = regionService.selectCacheRegionNameByCode(requestParam.getToStation());
        TicketPageQueryRespDTO ticketPageQueryRespDTO = new TicketPageQueryRespDTO();
        HashSet<Integer> typeClassSetRes = new HashSet<>();
        HashSet<Integer> TrainBrandSet = new HashSet<>();
        String starttime = DateUtil.format(requestParam.getDepartureDate(), "yyyy-MM-dd");
//        第一步-----开始
        List<TrainStationRelationDO> trainStationRelationDOS = cache.safeGetForList(TRAIN_PASS_ALL_CITY + String.join("-", starttime, startregion, endregion),
                ADVANCE_TICKET_DAY, TimeUnit.DAYS,
                () -> trainStationRelationMapper.queryByParam(starttime, startregion, endregion));
        ArrayList<TicketListDTO> ticketListDTOS = new ArrayList<>();
//        optimize 换成hash缓存
        for (TrainStationRelationDO tstationDO : trainStationRelationDOS) {
            TicketListDTO ticketListDTO = new TicketListDTO();
            ticketListDTOS.add(ticketListDTO);
            ticketListDTO.setTrainId(tstationDO.getTrainId().toString());
            TrainDO trainDO = trainService.getCacheTrainByTrainId(ticketListDTO.getTrainId());
            TrainBrandSet.addAll(Arrays.stream(trainDO.getTrainBrand().split(",")).map(Integer::parseInt).toList());
            trainDoToTicketListDTO(ticketListDTO, tstationDO, trainDO);
        }
//        第一步-----结束 找到了符合要求的列车
        List<Long> trainIds = ticketListDTOS.stream().map(t -> Long.parseLong(t.getTrainId())).toList();
        String collect = trainIds.stream().map(String::valueOf).sorted().collect(Collectors.joining("-"));
//       optimize 拆开换成hash 这里获取到座位信息
        List<CarriageDO> carriageDOS = cache.safeGetForList(TRAINCARRAGE + collect, ADVANCE_TICKET_DAY, TimeUnit.DAYS, () -> carrageMapper.selectList(new LambdaQueryWrapper<CarriageDO>()
                .in(CarriageDO::getTrainId, trainIds)
                .select(CarriageDO::getCarriageType, CarriageDO::getCarriageNumber, CarriageDO::getTrainId)));
//        第二步-----开始
        Map<String, Set<Integer>> trainToType = carriageDOS.stream()
                .collect(Collectors.groupingBy(a -> a.getTrainId().toString(), Collectors.mapping(CarriageDO::getCarriageType, Collectors.toSet())));
//        第二步-----结束
        for (TicketListDTO ticketListDTO : ticketListDTOS) {
            ArrayList<SeatClassDTO> seatclasses = new ArrayList<>();
            Set<Integer> seatTypeSet = trainToType.get(ticketListDTO.getTrainId());
            typeClassSetRes.addAll(seatTypeSet);
            for (Integer type : seatTypeSet) {
                SeatClassDTO seatClassDTO = new SeatClassDTO();
                seatClassDTO.setCandidate(false);
                seatClassDTO.setType(type);
                seatclasses.add(seatClassDTO);
            }
            ticketListDTO.setSeatClassList(seatclasses);
        }
        GeneCacheOfTicketForParchase(ticketListDTOS, trainToType);
        ticketPageQueryRespDTO.setTrainList(ticketListDTOS);
        ticketPageQueryRespDTO.setSeatClassTypeList(typeClassSetRes.stream().toList());
        ticketPageQueryRespDTO.setTrainBrandList(TrainBrandSet.stream().toList());
        return ticketPageQueryRespDTO;
    }

    /**
     * 三个for循环保存 列车id-开始车站-结束车站,field:seatType value seatcount
     * 将座位信息保存到缓存中
     */
    private void GeneCacheOfTicketForParchase(ArrayList<TicketListDTO> ticketListDTOS, Map<String, Set<Integer>> trainToType) {
        for (TicketListDTO ticketListDTO : ticketListDTOS) {
            String trainId = ticketListDTO.getTrainId();
            String departure = ticketListDTO.getDeparture();
            String arrival = ticketListDTO.getArrival();
            TrainDO trainDO = trainService.getCacheTrainByTrainId(trainId);
            String stratStation = trainDO.getStartStation();
            String endStation = trainDO.getEndStation();
            List<SeatClassDTO> seatClassList = ticketListDTO.getSeatClassList();
//       这里可以抽出来方法    通过列车id(每一个列车出发后都不一样,列车的唯一id是车次号码)  找到列车一条线路的所有节点,
//           列车id-开始站-经过站-type这是个参数,确定一个全部的座位号码
            List<TrainStationDO> trainStationDOS = cache.safeGetForList(TRAIN_PASS_ALL_STATION + trainId, ADVANCE_TICKET_DAY, TimeUnit.DAYS,
                    () -> trainStationMapper.queryBytrainId(trainId));
            ArrayList<String[]> startAndEndStation = geneListOfCache(trainStationDOS);
            for (String[] stationDOS : startAndEndStation) {
                String departureStation = stationDOS[0];
                String arrivalStation = stationDOS[1];
                Set<Integer> types = trainToType.get(trainId);
//                optimize 可以抽取出来作为方法
                for (Integer type : types) {
                    String KEY = REMAINTICKETOFSEAT_TRAIN + StrUtil.join("-", trainId, departureStation, arrivalStation);
                    Integer seatCount = cache.SafeGetOfHash(KEY, type,
                            () -> seatMapper.countByTrainIdAndSeatTypeAndArrivalAndDeparture(trainId, type, departureStation, arrivalStation));
                    log.info("缓存座位key:{},座位type{} 座位数量：{}", KEY, type, seatCount);
                }
            }
            Set<Integer> types = trainToType.get(trainId);
            for (SeatClassDTO seatClassDTO : seatClassList) {
                for (Integer type : types) {
                    if (seatClassDTO.getType() == type) {
//                        fixme 这里的stratStation  endStation改成 乘客出发站点和到达站点
                        String KEY = REMAINTICKETOFSEAT_TRAIN + StrUtil.join("-", trainId, departure, arrival);
                        Integer seatCount = cache.SafeGetOfHash(KEY, type,
                                () -> seatMapper.countByTrainIdAndSeatTypeAndArrivalAndDeparture(trainId, type, departure, arrival));
                        seatClassDTO.setQuantity(seatCount);
                    }

                }
            }
        }
    }
    /**
     * 1 2 3生成下面的集合,用于生成缓存中提供给购票的数据(座位数目)
     * 1, 2
     * 1, 3
     * 2, 3
     *
     * @param arr
     * @return
     */
    ArrayList<String[]> geneListOfCache(List<TrainStationDO> arr) {
        ArrayList<String[]> res = new ArrayList<>();
        arr.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getSequence())));
        for (int i = 0; i < arr.size(); i++) {
            for (int j = i + 1; j < arr.size(); j++) {
                if (i==j)continue;
                res.add(new String[]{arr.get(i).getDeparture(), arr.get(j).getDeparture()});
            }
        }
        return res;
    }

    /**
     * trainDo转TicketListDTO
     */
    private void trainDoToTicketListDTO(TicketListDTO ticketListDTO, TrainStationRelationDO stationRelationDO, TrainDO trainDO) {
        ticketListDTO.setDepartureFlag(stationRelationDO.getDepartureFlag());
        ticketListDTO.setArrivalFlag(stationRelationDO.getArrivalFlag());
        ticketListDTO.setArrival(stationRelationDO.getArrival());
        ticketListDTO.setTrainNumber(trainDO.getTrainNumber());
        ticketListDTO.setTrainType(trainDO.getTrainType());
        ticketListDTO.setTrainTags(Arrays.stream(trainDO.getTrainTag().split(",")).toList());
        ticketListDTO.setTrainBrand(trainDO.getTrainBrand());
        ticketListDTO.setDeparture(stationRelationDO.getDeparture());
        ticketListDTO.setArrivalTime(DateUtil.format(trainDO.getArrivalTime(), "yyyy-MM-dd HH:mm"));
        ticketListDTO.setDepartureTime(DateUtil.format(trainDO.getDepartureTime(), "yyyy-MM-dd HH:mm"));
        ticketListDTO.setSaleStatus(trainDO.getSaleStatus());
        ticketListDTO.setDuration(DateUtil.formatBetween(stationRelationDO.getArrivalTime(), stationRelationDO.getDepartureTime(), BetweenFormatter.Level.HOUR));
        ticketListDTO.setDaysArrived(DateUtil.formatBetween(stationRelationDO.getArrivalTime(), stationRelationDO.getDepartureTime(), BetweenFormatter.Level.DAY).charAt(0) - '0');
        ticketListDTO.setSaleTime(DateUtil.format(trainDO.getSaleTime(), "yyyy-MM-dd HH:mm"));
    }
    /**
     * todo 跳过
     * 根据条件分页查询车票(高性能)
     * @param requestParam 分页查询车票请求参数
     * @return 查询车票返回结果
     */
    @Override
    public TicketPageQueryRespDTO pageListTicketQueryV2(TicketPageQueryReqDTO requestParam) {
        return null;
    }


    /**
     * 购买车票v1
     *
     * @param requestParam 车票购买请求参数
     * @return 订单号
     */
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public TicketPurchaseRespDTO purchaseTicketsV1(PurchaseTicketReqDTO requestParam) {
//        先过滤
        chainsContext.execute("TrainPurchaseTicketChainFilter", requestParam);
        String lockKey = String.format(String.format(LOCK_PURCHASE_TICKETS, requestParam.getTrainId()));
        RLock lock = redissonClient.getLock(lockKey);
        boolean b = lock.tryLock();
        if (!b) throw new ClientException("请重试");
        try {
            TicketServiceImpl bean = ApplicationContextHolder.getBean(this.getClass());
            return bean.executePurchaseTickets(requestParam);
        } finally {
            lock.unlock();
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public TicketPurchaseRespDTO executePurchaseTickets(PurchaseTicketReqDTO requestParam) {
        String trainId = requestParam.getTrainId();
        TrainDO trainDO = trainService.getCacheTrainByTrainId(trainId);
//        选出座位
        List<TrainPurchaseTicketRespDTO> trainPurchaseTicketResults = seatTypeSelector.select(trainDO.getTrainType(), requestParam);
//        生成车票
        List<TicketDO> ticketDOList = trainPurchaseTicketResults.stream()
                .map(each -> TicketDO.builder()
                        .username(UserContext.getUserName())
                        .trainId(Long.parseLong(requestParam.getTrainId()))
                        .carriageNumber(each.getCarriageNumber())
                        .seatNumber(each.getSeatNumber())
                        .passengerId(each.getPassengerId())
                        .ticketStatus(TicketStatusEnum.UNPAID.getCode())
                        .build())
                .toList();
        TicketServiceImpl bean = ApplicationContextHolder.getBean(TicketServiceImpl.class);
//        保存车票
        bean.saveBatch(ticketDOList);
        Res<String> ticketOrderResult;
        List<TicketOrderDetailRespDTO> ticketOrderDetailResults;
        try {
            List<TicketOrderItemCreateRemoteReqDTO> orderItemCreateRemoteReqDTOList = trainPurchaseTicketResults.stream()
                    .map(this::buildTicketOrderItemCreateRemoteReqDTO)
                    .collect(Collectors.toList());
            ticketOrderDetailResults = trainPurchaseTicketResults.stream()
                    .map(this::buildTicketOrderDetailRespDTO)
                    .collect(Collectors.toList());
            LambdaQueryWrapper<TrainStationRelationDO> queryWrapper = Wrappers.lambdaQuery(TrainStationRelationDO.class)
                    .eq(TrainStationRelationDO::getTrainId, trainId)
                    .eq(TrainStationRelationDO::getDeparture, requestParam.getDeparture())
                    .eq(TrainStationRelationDO::getArrival, requestParam.getArrival());
            TrainStationRelationDO trainStationRelationDO = trainStationRelationMapper.selectOne(queryWrapper);
            TicketOrderCreateRemoteReqDTO orderCreateRemoteReqDTO = buildTicketOrderCreateRemoteReqDTO(requestParam, trainDO, trainStationRelationDO, orderItemCreateRemoteReqDTOList);
            ticketOrderResult = ticketOrderRemoteService.createTicketOrder(orderCreateRemoteReqDTO);
            if (!ticketOrderResult.isSuccess() || StrUtil.isBlank(ticketOrderResult.getData())) {
                log.error("订单服务调用失败，返回结果：{}", ticketOrderResult.getMessage());
                throw new ServiceException("订单服务调用失败");
            }
        } catch (Throwable ex) {
            log.error("远程调用订单服务创建错误，请求参数：{}", JSON.toJSONString(requestParam), ex);
            throw ex;
        }
        HashOperations<String, Integer, Integer> hashOperations = cache.getInstance().opsForHash();
//      获取列车Id,获取出发点 到达点，生成直达线路数组
//        获取座位类型，删除对应的缓存
        for (TicketOrderDetailRespDTO ticketOrderDetailResult : ticketOrderDetailResults) {
            Integer seatType = ticketOrderDetailResult.getSeatType();
            String seatNumber = ticketOrderDetailResult.getSeatNumber();
            String carriageNumber = ticketOrderDetailResult.getCarriageNumber();
            for (RouteDTO routeDTO : trainStationService.listTakeoutTrainStationRoute(trainId, requestParam.getDeparture(), requestParam.getArrival())) {
                String startStation = routeDTO.getStartStation();
                String endStation = routeDTO.getEndStation();
//                并且将对应的trianId   seatnumber  seattype starstation endstation改变状态
                SeatDO seatDO = new SeatDO();
                seatDO.setSeatStatus(SeatStatusEnum.LOCKED.getCode());
                int update = seatMapper.update(new SeatDO(), new LambdaQueryWrapper<SeatDO>().eq(SeatDO::getSeatType, seatType).eq(SeatDO::getTrainId, trainId)
                        .eq(SeatDO::getStartStation, startStation).eq(SeatDO::getEndStation, endStation).eq(SeatDO::getCarriageNumber, carriageNumber));
                if (update<1)throw new ServiceException("当前座位已经被抢占");
                String key = String.join("-", trainId, startStation, endStation);
                log.info("缓存key:{}", REMAINTICKETOFSEAT_TRAIN + key);
//                 修改为减少缓存
                Long decrement = hashOperations.increment(REMAINTICKETOFSEAT_TRAIN + key, seatType, -1);
//                Long delete = hashOperations.delete(REMAINTICKETOFSEAT_TRAIN + key, seatType);
                if (decrement < 1)throw new ServiceException("购票缓存减少失败");
                else log.info("购票缓存减少成功");
            }
        }

        return new TicketPurchaseRespDTO(ticketOrderResult.getData(), ticketOrderDetailResults);
    }


    /**
     * 购买车票v2(高性能)
     *
     * @param requestParam 车票购买请求参数
     * @return 订单号
     */
    @Override
    public TicketPurchaseRespDTO purchaseTicketsV2(PurchaseTicketReqDTO requestParam) {
        return null;
    }

    /**
     * 取消车票
     *
     * @param requestParam 车票取消请求参数
     */
    @Override
    public void cancelTicketOrder(CancelTicketOrderReqDTO requestParam) {

    }

    /**
     * 查询支付单详情查询
     *
     * @param orderSn 订单号
     * @return 支付单详情查询
     */
    @Override
    public PayInfoRespDTO getPayInfo(String orderSn) {
        return null;
    }

    /**
     * 公共退款接口
     *
     * @param requestParam 退款请求参数
     * @return 退款返回详情
     */
    @Override
    public RefundTicketRespDTO commonTicketRefund(RefundTicketReqDTO requestParam) {
        return null;
    }

    /**
     * 设置订单关联的座位状态为0
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean ResetSeatStatus(List<ResetSeatDTO> dto) {
        for (ResetSeatDTO resetSeatDTO : dto) {
            Integer unlock = seatService.unlock(String.valueOf(resetSeatDTO.getTrainId()),
                    resetSeatDTO.getStartStation(),
                    resetSeatDTO.getEndStation(),
                    resetSeatDTO.getSeatType());
            if (unlock < 1) return false;
            // 删除缓存，不不只是一个，而是多个，删除经过的车站缓存
            List<RouteDTO> routeDTOS = trainStationService.listTakeoutTrainStationRoute(String.valueOf(resetSeatDTO.getTrainId()), resetSeatDTO.getStartStation(),
                    resetSeatDTO.getEndStation());
            for (RouteDTO routeDTO : routeDTOS) {
                String join = String.join("-", String.valueOf(resetSeatDTO.getTrainId()),
                        routeDTO.getStartStation(), routeDTO.getEndStation());
                log.info(REMAINTICKETOFSEAT_TRAIN + join);
                HashOperations hashOperations = cache.getInstance().opsForHash();
                Long increment = hashOperations.increment(REMAINTICKETOFSEAT_TRAIN + join, resetSeatDTO.getSeatType(), 1);
                if (Boolean.FALSE.equals(increment)) throw new ServiceException("添加座位数量缓存异常");
                else log.info("-----------添加座位数量缓存成功------------");
            }
        }
        return true;
    }


    private TicketOrderDetailRespDTO buildTicketOrderDetailRespDTO(TrainPurchaseTicketRespDTO each) {
        return TicketOrderDetailRespDTO.builder()
                .amount(each.getAmount())
                .carriageNumber(each.getCarriageNumber())
                .seatNumber(each.getSeatNumber())
                .idCard(each.getIdCard())
                .idType(each.getIdType())
                .seatType(each.getSeatType())
                .ticketType(each.getUserType())
                .realName(each.getRealName())
                .build();
    }

    private TicketOrderItemCreateRemoteReqDTO buildTicketOrderItemCreateRemoteReqDTO(TrainPurchaseTicketRespDTO each) {
        return TicketOrderItemCreateRemoteReqDTO.builder()
                .amount(each.getAmount())
                .carriageNumber(each.getCarriageNumber())
                .seatNumber(each.getSeatNumber())
                .idCard(each.getIdCard())
                .idType(each.getIdType())
                .phone(each.getPhone())
                .seatType(each.getSeatType())
                .ticketType(each.getUserType())
                .realName(each.getRealName())
                .build();
    }

    private static TicketOrderCreateRemoteReqDTO buildTicketOrderCreateRemoteReqDTO(PurchaseTicketReqDTO requestParam,
                                                                                    TrainDO trainDO,
                                                                                    TrainStationRelationDO trainStationRelationDO,
                                                                                    List<TicketOrderItemCreateRemoteReqDTO> orderItemCreateRemoteReqDTOList) {
        return TicketOrderCreateRemoteReqDTO.builder()
                .departure(requestParam.getDeparture())
                .arrival(requestParam.getArrival())
                .orderTime(new Date())
                .source(SourceEnum.INTERNET.getCode())
                .trainNumber(trainDO.getTrainNumber())
                .departureTime(trainStationRelationDO.getDepartureTime())
                .arrivalTime(trainStationRelationDO.getArrivalTime())
                .ridingDate(trainStationRelationDO.getDepartureTime())
                .userId(UserContext.getUserId())
                .username(UserContext.getUserName())
                .trainId(Long.parseLong(requestParam.getTrainId()))
                .ticketOrderItems(orderItemCreateRemoteReqDTOList)
                .build();
    }
}
