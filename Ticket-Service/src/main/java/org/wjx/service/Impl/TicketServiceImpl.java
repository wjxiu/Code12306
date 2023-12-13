package org.wjx.service.Impl;

import cn.hutool.core.date.BetweenFormatter;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wjx.Exception.ClientException;
import org.wjx.Exception.ServiceException;
import org.wjx.Res;
import org.wjx.common.TicketChainMarkEnum;
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
import org.wjx.enums.SourceEnum;
import org.wjx.enums.TicketStatusEnum;
import org.wjx.filter.AbstractFilterChainsContext;
import org.wjx.handler.select.TrainSeatTypeSelector;
import org.wjx.remote.TicketOrderRemoteService;
import org.wjx.remote.dto.ResetSeatDTO;
import org.wjx.remote.dto.TicketOrderCreateRemoteReqDTO;
import org.wjx.remote.dto.TicketOrderItemCreateRemoteReqDTO;
import org.wjx.service.SeatService;
import org.wjx.service.TicketService;
import org.wjx.user.core.ApplicationContextHolder;
import org.wjx.user.core.UserContext;
import org.wjx.utils.BeanUtil;

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
    final TrainMapper trainMapper;
    final TicketOrderRemoteService ticketOrderRemoteService;
    final TrainStationMapper trainStationMapper;
    final CarrageMapper carrageMapper;
    final TrainStationPriceMapper priceMapper;
    final RegionMapper regionMapper;
    final SeatMapper seatMapper;
    final TrainSeatTypeSelector seatTypeSelector;
    final AbstractFilterChainsContext filterChainsContext;
    final SeatService seatService;


    /**
     * 根据条件分页查询车票
     *
     * @param requestParam 分页查询车票请求参数
     * @return 查询车票返回结果
     */
    @Override
    @SneakyThrows
    public TicketPageQueryRespDTO pageListTicketQueryV1(TicketPageQueryReqDTO requestParam) {
        //大致流程:
//        1.查询train_station_relation表,查出发地-目的地的列车id,
//        2.根据列车id 查询carrage表,查出列车对应的座位类型
//        3.根据出发地-目的地查询train_station,主要获得列车站台之间的顺序
//        4根据站台顺序拼接二维数组(a-b-c-d-e :[[a,b],[a,c],[a,d],[a,e],[b-c]....])
//        5.根据列车Id,二维数组座位type,三个for循环,遍历seat表查询座位的数量,保存缓存
        TicketPageQueryRespDTO ticketPageQueryRespDTO = gene(requestParam);
        List<TicketListDTO> gene = ticketPageQueryRespDTO.getTrainList();
        ticketPageQueryRespDTO.setTrainList(gene);
        ticketPageQueryRespDTO.setDepartureStationList(parseDepartureStationList(gene));
        ticketPageQueryRespDTO.setArrivalStationList(parseArrivalStationList(gene));
        setPrice(ticketPageQueryRespDTO);
        return ticketPageQueryRespDTO;
    }

    /**
     * 给所有结果查出票价
     *
     * @param ticketPageQueryRespDTO
     */
    private void setPrice(TicketPageQueryRespDTO ticketPageQueryRespDTO) {
        List<TicketListDTO> trainList = ticketPageQueryRespDTO.getTrainList();
        for (TicketListDTO ticketListDTO : trainList) {
            String departure = ticketListDTO.getDeparture();
            String arrival = ticketListDTO.getArrival();
            String trainId = ticketListDTO.getTrainId();
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
        String startregion = cache.SafeGetOfHash(CODE_TRAIN_NAME, requestParam.getFromStation(), () -> {
            return regionMapper.selectRegionNameByCode(requestParam.getFromStation());
        });
        String endregion = cache.SafeGetOfHash(CODE_TRAIN_NAME, requestParam.getToStation(), () -> {
            return regionMapper.selectRegionNameByCode(requestParam.getToStation());
        });
        TicketPageQueryRespDTO ticketPageQueryRespDTO = new TicketPageQueryRespDTO();
        HashSet<Integer> typeClassSetRes = new HashSet<>();
        StringBuffer sb = new StringBuffer();
        String starttime = DateUtil.format(requestParam.getDepartureDate(), "yyyy-MM-dd");
        List<TrainStationRelationDO> trainStationRelationDOS = cache.safeGetForList(TRAIN_PASS_ALL_CITY + String.join("-", starttime, startregion, endregion),
                ADVANCE_TICKET_DAY, TimeUnit.DAYS,
                () -> {
                    return trainStationRelationMapper.queryByParam(starttime, startregion, endregion);
                });
//        找到了符合要求的列车
        ArrayList<TicketListDTO> ticketListDTOS = new ArrayList<>();
        for (TrainStationRelationDO tstationDO : trainStationRelationDOS) {
            TicketListDTO ticketListDTO = new TicketListDTO();
            ticketListDTOS.add(ticketListDTO);
            ticketListDTO.setTrainId(tstationDO.getTrainId().toString());
            TrainDO trainDO = cache.safeGet(TRAIN_INFO_BY_TRAINID + ticketListDTO.getTrainId(), ADVANCE_TICKET_DAY, TimeUnit.DAYS, () -> {
                return trainMapper.selectById(ticketListDTO.getTrainId());
            });
            sb.append(trainDO.getTrainBrand()).append(",");
            trainDoToTicketListDTO(ticketListDTO, tstationDO, trainDO);
        }
        List<Long> list1 = ticketListDTOS.stream().map(t -> Long.parseLong(t.getTrainId())).toList();
        String collect = list1.stream().map(String::valueOf).sorted().collect(Collectors.joining("-"));
//        这里获取到座位信息
        List<CarriageDO> carriageDOS = cache.safeGetForList(TRAINCARRAGE + collect, ADVANCE_TICKET_DAY, TimeUnit.DAYS, () -> {
            return carrageMapper.selectList(new LambdaQueryWrapper<CarriageDO>()
                    .in(CarriageDO::getTrainId, list1)
                    .select(CarriageDO::getCarriageType, CarriageDO::getCarriageNumber, CarriageDO::getTrainId, CarriageDO::getSeatCount));
        });
        Map<String, Set<Integer>> trainToType = carriageDOS.stream()
                .collect(Collectors.groupingBy(a -> a.getTrainId().toString(), Collectors.mapping(CarriageDO::getCarriageType, Collectors.toSet())));
        GeneCacheOfTicketForParchase(ticketListDTOS, trainToType);

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
        setQuantity(carriageDOS, ticketListDTOS);
        ticketPageQueryRespDTO.setTrainList(ticketListDTOS);
        ticketPageQueryRespDTO.setSeatClassTypeList(typeClassSetRes.stream().toList());
        ticketPageQueryRespDTO.setTrainBrandList(Arrays.stream(sb.toString().split(",")).map(Integer::parseInt).distinct().toList());
        return ticketPageQueryRespDTO;
    }

    /**
     * 三个for循环保存 列车id-开始车站-结束车站,field:seatType value seatcount
     * 将座位信息保存到缓存中
     */
    private void GeneCacheOfTicketForParchase(ArrayList<TicketListDTO> ticketListDTOS, Map<String, Set<Integer>> trainToType) {
        HashOperations<String, Integer, Integer> hashOperations = cache.getInstance().opsForHash();
        for (TicketListDTO ticketListDTO : ticketListDTOS) {
            String trainId = ticketListDTO.getTrainId();
//           通过列车id(每一个列车出发后都不一样,列车的唯一id是车次号码)  找到列车一条线路的所有节点,
//           列车id-开始站-经过站-type这是个参数,确定一个全部的座位号码
            List<TrainStationDO> trainStationDOS = cache.safeGetForList(TRAIN_PASS_ALL_STATION + trainId, ADVANCE_TICKET_DAY, TimeUnit.DAYS, () -> {
                return trainStationMapper.queryBytrainId(trainId);
            });
            ArrayList<String[]> startAndEndStation = geneListOfCache(trainStationDOS);
            for (String[] stationDOS : startAndEndStation) {
                String stratstation = stationDOS[0];
                String endstation = stationDOS[1];
                Set<Integer> types = trainToType.get(trainId);
//                optimize 可以抽取出来作为方法
                for (Integer type : types) {
                    String KEY = REMAINTICKETOFSEAT_TRAIN + StrUtil.join("-", trainId, stratstation, endstation);
                    cache.SafeGetOfHash(KEY, type, () -> {
                        return seatMapper.countByTrainIdAndSeatTypeAndArrivalAndDeparture(trainId, type, stratstation, endstation);
                    });
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
                res.add(new String[]{arr.get(i).getDeparture(), arr.get(j).getDeparture()});
            }
        }
        return res;
    }

    /**
     * 设置最终结果的中的座位数量，座位数量已经保存到缓存中了
     *
     * @param carriageDOS
     * @param ticketListDTOS
     */
    private void setQuantity(List<CarriageDO> carriageDOS, ArrayList<TicketListDTO> ticketListDTOS) {
        Map<AbstractMap.SimpleEntry<String, Integer>, Integer> simpleEntryIntegerMap = groupByTrainIdAndCarriageType(carriageDOS);
        for (TicketListDTO ticketListDTO : ticketListDTOS) {
            String trainId = ticketListDTO.getTrainId();
            for (SeatClassDTO seatClassDTO : ticketListDTO.getSeatClassList()) {
                AbstractMap.SimpleEntry<String, Integer> key = new AbstractMap.SimpleEntry<>(trainId, seatClassDTO.getType());
                Integer value = simpleEntryIntegerMap.get(key);
                seatClassDTO.setQuantity(value);
            }
        }
    }

    private List<String> parseDepartureStationList(List<TicketListDTO> gene) {
        return gene.stream().map(TicketListDTO::getDeparture).collect(Collectors.toSet()).stream().toList();
    }

    private List<String> parseArrivalStationList(List<TicketListDTO> gene) {
        return gene.stream().map(TicketListDTO::getArrival).collect(Collectors.toSet()).stream().toList();
    }

    private void trainDoToTicketListDTO(TicketListDTO ticketListDTO, TrainStationRelationDO stationRelationDO, TrainDO trainDO) {
        ticketListDTO.setDepartureFlag(stationRelationDO.getDepartureFlag());
        ticketListDTO.setArrivalFlag(stationRelationDO.getArrivalFlag());
        ticketListDTO.setArrival(stationRelationDO.getArrival());
        ticketListDTO.setTrainNumber(trainDO.getTrainNumber());
        ticketListDTO.setTrainType(trainDO.getTrainType());
        ticketListDTO.setTrainTags(Arrays.stream(trainDO.getTrainTag().split(",")).toList());
        ticketListDTO.setTrainBrand(trainDO.getTrainBrand());
        ticketListDTO.setDeparture(stationRelationDO.getDeparture());
        ticketListDTO.setArrivalTime(DateUtil.format(stationRelationDO.getArrivalTime(), "yyyy-MM-dd HH:mm"));
        ticketListDTO.setDepartureTime(DateUtil.format(stationRelationDO.getDepartureTime(), "yyyy-MM-dd HH:mm"));
        ticketListDTO.setSaleStatus(trainDO.getSaleStatus());
        ticketListDTO.setDuration(DateUtil.formatBetween(stationRelationDO.getArrivalTime(), stationRelationDO.getDepartureTime(), BetweenFormatter.Level.HOUR));
        ticketListDTO.setDaysArrived(DateUtil.formatBetween(stationRelationDO.getArrivalTime(), stationRelationDO.getDepartureTime(), BetweenFormatter.Level.DAY).charAt(0) - '0');
        ticketListDTO.setSaleTime(DateUtil.format(trainDO.getSaleTime(), "yyyy-MM-dd HH:mm"));
    }


    private Map<AbstractMap.SimpleEntry<String, Integer>, Integer> groupByTrainIdAndCarriageType(List<CarriageDO> carriageDOList) {
        return carriageDOList.stream()
                .collect(Collectors.toMap(
                        carriage -> new AbstractMap.SimpleEntry<>(carriage.getTrainId().toString(), carriage.getCarriageType()),
                        CarriageDO::getSeatCount,
                        Integer::sum
                ));
    }

    /**
     * todo 跳过
     * 根据条件分页查询车票(高性能)
     *
     * @param requestParam 分页查询车票请求参数
     * @return 查询车票返回结果
     */
    @Override
    public TicketPageQueryRespDTO pageListTicketQueryV2(TicketPageQueryReqDTO requestParam) {
//        通过职责链模式过滤参数
        chainsContext.execute(TicketChainMarkEnum.TRAIN_QUERY_FILTER.name(), requestParam);
        StringRedisTemplate instance = (StringRedisTemplate) cache.getInstance();

        return null;
    }


    /**
     * 购买车票v1
     *
     * @param requestParam 车票购买请求参数
     * @return 订单号
     */
    @Override
    public TicketPurchaseRespDTO purchaseTicketsV1(PurchaseTicketReqDTO requestParam) {
//        先过滤
        chainsContext.execute("TrainPurchaseTicketChainFilter", requestParam);
        String lockKey = String.format(String.format(LOCK_PURCHASE_TICKETS, requestParam.getTrainId()));
        RLock lock = redissonClient.getLock(lockKey);
        boolean b = lock.tryLock();
        if (!b) throw new ClientException("请重试");
        try {
            return executePurchaseTickets(requestParam);
        } finally {
            lock.unlock();
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public TicketPurchaseRespDTO executePurchaseTickets(PurchaseTicketReqDTO requestParam) {
        List<TicketOrderDetailRespDTO> ticketOrderDetailResults = new ArrayList<>();
        String trainId = requestParam.getTrainId();
        TrainDO trainDO = cache.safeGet(
                "frame.redisticket-service:train:" + trainId,
                ADVANCE_TICKET_DAY,
                TimeUnit.DAYS, () -> trainMapper.selectById(trainId));
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
        try {
            List<TicketOrderItemCreateRemoteReqDTO> orderItemCreateRemoteReqDTOList = new ArrayList<>();
            trainPurchaseTicketResults.forEach(each -> {
                TicketOrderItemCreateRemoteReqDTO orderItemCreateRemoteReqDTO = bean.buildTicketOrderItemCreateRemoteReqDTO(each);
                TicketOrderDetailRespDTO ticketOrderDetailRespDTO = bean.buildTicketOrderDetailRespDTO(each);
                orderItemCreateRemoteReqDTOList.add(orderItemCreateRemoteReqDTO);
                ticketOrderDetailResults.add(ticketOrderDetailRespDTO);
            });
            LambdaQueryWrapper<TrainStationRelationDO> queryWrapper = Wrappers.lambdaQuery(TrainStationRelationDO.class)
                    .eq(TrainStationRelationDO::getTrainId, trainId)
                    .eq(TrainStationRelationDO::getDeparture, requestParam.getDeparture())
                    .eq(TrainStationRelationDO::getArrival, requestParam.getArrival());
            TrainStationRelationDO trainStationRelationDO = trainStationRelationMapper.selectOne(queryWrapper);
            TicketOrderCreateRemoteReqDTO orderCreateRemoteReqDTO = bean.buildTicketOrderCreateRemoteReqDTO(requestParam, trainDO,
                    trainStationRelationDO, orderItemCreateRemoteReqDTOList);
            ticketOrderResult = ticketOrderRemoteService.createTicketOrder(orderCreateRemoteReqDTO);
            if (!ticketOrderResult.isSuccess() || StrUtil.isBlank(ticketOrderResult.getData())) {
                log.error("订单服务调用失败，返回结果：{}", ticketOrderResult.getMessage());
                throw new ServiceException("订单服务调用失败");
            }
        } catch (Throwable ex) {
            log.error("远程调用订单服务创建错误，请求参数：{}", JSON.toJSONString(requestParam), ex);
            throw ex;
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
        RedisTemplate instance = cache.getInstance();
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
            seatService.unlock(String.valueOf(  resetSeatDTO.getTrainId()),
                                                resetSeatDTO.getStartStation(),
                                                resetSeatDTO.getEndStation(),
                                                resetSeatDTO.getSeatType());
            SeatDO convert = BeanUtil.convert(resetSeatDTO, SeatDO.class);
//            optimize 可以设置为别的状态，之后发送到延迟队列，再改为0
            convert.setSeatStatus(0);
            int update = seatMapper.update(convert, new LambdaQueryWrapper<SeatDO>()
                    .eq(SeatDO::getSeatNumber, resetSeatDTO.getSeatNumber())
                    .eq(SeatDO::getTrainId, resetSeatDTO.getTrainId())
                    .eq(SeatDO::getSeatType, resetSeatDTO.getSeatType()));
            if (update < 1) return false;
        }
        return true;
    }


    private static TicketOrderDetailRespDTO buildTicketOrderDetailRespDTO(TrainPurchaseTicketRespDTO each) {
        TicketOrderDetailRespDTO ticketOrderDetailRespDTO = TicketOrderDetailRespDTO.builder()
                .amount(each.getAmount())
                .carriageNumber(each.getCarriageNumber())
                .seatNumber(each.getSeatNumber())
                .idCard(each.getIdCard())
                .idType(each.getIdType())
                .seatType(each.getSeatType())
                .ticketType(each.getUserType())
                .realName(each.getRealName())
                .build();
        return ticketOrderDetailRespDTO;
    }

    private static TicketOrderItemCreateRemoteReqDTO buildTicketOrderItemCreateRemoteReqDTO(TrainPurchaseTicketRespDTO each) {
        TicketOrderItemCreateRemoteReqDTO orderItemCreateRemoteReqDTO = TicketOrderItemCreateRemoteReqDTO.builder()
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
        return orderItemCreateRemoteReqDTO;
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
