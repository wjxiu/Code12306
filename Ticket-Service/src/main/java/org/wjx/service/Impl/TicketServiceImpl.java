package org.wjx.service.Impl;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.wjx.common.TicketChainMarkEnum;
import org.wjx.core.CacheLoader;
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
import org.wjx.filter.AbstractFilterChainsContext;
import org.wjx.service.TicketService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author xiu
 * @create 2023-11-28 15:16
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    final AbstractFilterChainsContext chainsContext;
    final SafeCache cache;
    final TrainStationRelationMapper trainStationRelationMapper;
    final TrainMapper trainMapper;
    final TrainStationMapper trainStationMapper;
    final CarrageMapper carrageMapper;
    final TrainStationPriceMapper priceMapper;
    final RegionMapper regionMapper;
    final SeatMapper seatMapper;


    /**
     * 根据条件分页查询车票
     *
     * @param requestParam 分页查询车票请求参数
     * @return 查询车票返回结果
     */
    @Override
    @SneakyThrows
    public TicketPageQueryRespDTO pageListTicketQueryV1(TicketPageQueryReqDTO requestParam) {
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
                String trainStationPriceDOString = cache.SafeGetOfHash("ticket:sevice:train:price:" +
                        String.join(":", departure, arrival, trainId), seatClassDTO.getType().toString(), String.class, (CacheLoader<String>) () -> {
                    TrainStationPriceDO trainStationPriceDO = priceMapper.selectOne(new LambdaQueryWrapper<TrainStationPriceDO>()
                            .eq(TrainStationPriceDO::getDeparture, departure)
                            .eq(TrainStationPriceDO::getArrival, arrival)
                            .eq(TrainStationPriceDO::getTrainId, trainId)
                            .eq(TrainStationPriceDO::getSeatType, seatClassDTO.getType())
                            .select(TrainStationPriceDO::getPrice));
                    return trainStationPriceDO.getPrice().toString();
                });
                Long price = JSON.parseObject(trainStationPriceDOString, Long.class);
                BigDecimal bigDecimal = new BigDecimal(price / 100).setScale(2, RoundingMode.HALF_UP);
                seatClassDTO.setPrice(bigDecimal);
            }
        }
    }

    /**
     * 生成 TicketPageQueryRespDTO的 List<TicketListDTO> 和SeatClassTypeList
     *
     * @param requestParam 请求参数
     * @return TicketPageQueryRespDTO的
     */
    private TicketPageQueryRespDTO gene(TicketPageQueryReqDTO requestParam) {
        String startregion = cache.SafeGetOfHash("ticket:sevice:code:trainName", requestParam.getFromStation(), String.class, () -> {
            return regionMapper.selectRegionNameByCode(requestParam.getFromStation());
        });
        String endregion = cache.SafeGetOfHash("ticket:sevice:code:trainName", requestParam.getToStation(), String.class, () -> {
            return regionMapper.selectRegionNameByCode(requestParam.getToStation());
        });
        TicketPageQueryRespDTO ticketPageQueryRespDTO = new TicketPageQueryRespDTO();
        HashSet<Integer> typeClassSetRes = new HashSet<>();
        StringBuffer sb = new StringBuffer();
        String starttime = DateUtil.format(requestParam.getDepartureDate(), "yyyy-MM-dd");
        String list = cache.safeGet("ticket:sevice:train:query" + String.join("-", starttime, startregion, endregion),
                String.class, 15L, TimeUnit.DAYS,
                () -> JSON.toJSONString(trainStationRelationMapper.queryByParam(starttime, startregion, endregion)));
        List<TrainStationRelationDO> trainStationRelationDOS = JSON.parseArray(list, TrainStationRelationDO.class);
//        找到了符合要求的列车
        ArrayList<TicketListDTO> ticketListDTOS = new ArrayList<>();
        for (TrainStationRelationDO tstationDO : trainStationRelationDOS) {
            TicketListDTO ticketListDTO = new TicketListDTO();
            ticketListDTOS.add(ticketListDTO);
            ticketListDTO.setTrainId(tstationDO.getTrainId().toString());
            String trainDoString = cache.safeGet("ticket:sevice:train:id:" + ticketListDTO.getTrainId(), String.class, 15L, TimeUnit.DAYS, () -> {
                return JSON.toJSONString(trainMapper.selectById(ticketListDTO.getTrainId()));
            });
            TrainDO trainDO = JSON.parseObject(trainDoString, TrainDO.class);
            sb.append(trainDO.getTrainBrand()).append(",");
            trainDoToTicketListDTO(ticketListDTO, tstationDO, trainDO);
        }
        List<Long> list1 = ticketListDTOS.stream().map(t -> Long.parseLong(t.getTrainId())).toList();
        String collect = list1.stream().map(String::valueOf).collect(Collectors.joining("-"));

        String carriageDOString = cache.safeGet("ticket:sevice:train:carriage:" + collect, String.class, 15L, TimeUnit.DAYS, () -> {
            return JSON.toJSONString(carrageMapper.selectList(new LambdaQueryWrapper<CarriageDO>()
                    .in(CarriageDO::getTrainId, list1)
                    .select(CarriageDO::getCarriageType, CarriageDO::getCarriageNumber, CarriageDO::getTrainId, CarriageDO::getSeatCount))
            );
        });
//        这里获取到座位信息
        List<CarriageDO> carriageDOS = JSON.parseArray(carriageDOString, CarriageDO.class);
        Map<String, Set<Integer>> trainToType = carriageDOS.stream()
                .collect(Collectors.groupingBy(a -> a.getTrainId().toString(),
                        Collectors.mapping(CarriageDO::getCarriageType, Collectors.toSet())));

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
     */
    private void GeneCacheOfTicketForParchase(ArrayList<TicketListDTO> ticketListDTOS, Map<String, Set<Integer>> trainToType) {
        HashOperations hashOperations = cache.getInstance().opsForHash();
        for (TicketListDTO ticketListDTO : ticketListDTOS) {
            String trainId = ticketListDTO.getTrainId();
//           通过列车id(每一个列车出发后都不一样,列车的唯一id是车次号码)  找到列车一条线路的所有节点,
//           列车id-开始站-经过站-type这是个参数,确定一个全部的座位号码
         String  trainStationDOSTring=   cache.safeGet("ticket-service:train:"+trainId,String.class,15L,TimeUnit.DAYS,()->{
                return JSON.toJSONString(trainStationMapper.queryBytrainId(trainId));
            });
            List<TrainStationDO> trainStationDOS =JSON.parseArray(trainStationDOSTring,TrainStationDO.class);
            ArrayList<String[]> trainStationDOS1 = geneListOfCache(trainStationDOS);
            for (String[] stationDOS : trainStationDOS1) {
                String stratstation = stationDOS[0];
                String endstation = stationDOS[1];
                Set<Integer> types = trainToType.get(trainId);
                for (Integer type : types) {
                    Object o = hashOperations.get("ticket-service:train-station-price:" + String.join("-", trainId, stratstation, endstation), type.toString());
                    if (o == null) {
                        Integer count = seatMapper.countByTrainIdAndSeatTypeAndArrivalAndDeparture(trainId, type, stratstation, endstation);
                        hashOperations.put("ticket-service:train-station-price:" + String.join("-", trainId, stratstation, endstation), type.toString(), count.toString());
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
                res.add(new String[]{arr.get(i).getDeparture(), arr.get(j).getDeparture()});
            }
        }
        return res;
    }

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
        List<TicketListDTO> seatResults = getTicketListDTOS(instance, requestParam);

        return null;
    }

    private List<TicketListDTO> getTicketListDTOS(StringRedisTemplate instance, TicketPageQueryReqDTO requestParam) {
//todo
        return null;
    }

    /**
     * 购买车票v1
     *
     * @param requestParam 车票购买请求参数
     * @return 订单好
     */
    @Override
    public TicketPurchaseRespDTO purchaseTicketsV1(PurchaseTicketReqDTO requestParam) {
        return null;
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
}
