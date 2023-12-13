package org.wjx.filter.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.wjx.Exception.ClientException;
import org.wjx.core.SafeCache;
import org.wjx.dao.mapper.SeatMapper;
import org.wjx.dto.req.PurchaseTicketReqDTO;
import org.wjx.handler.DTO.PurchaseTicketPassengerDetailDTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.wjx.constant.RedisKeyConstant.REMAINTICKETOFSEAT_TRAIN;
import static org.wjx.constant.SystemConstant.ADVANCE_TICKET_DAY;

/**
 *  座位数量信息加载
 * 购票流程过滤器之验证列车站点库存是否充足
 *
 * @author xiu
 * @create 2023-12-04 19:44
 */
@Component@Slf4j
@RequiredArgsConstructor
public class TrainPurchaseTicketParamStockChainHandler implements TrainPurchaseTicketChainFilter<PurchaseTicketReqDTO>{
    final SafeCache cache;
    final SeatMapper seatMapper;
    /**
     *
     *
     * @param requestParam 被过滤的数据
     */
    @Override
    public void handle(PurchaseTicketReqDTO requestParam) {
        List<String> chooseSeats = requestParam.getChooseSeats();
        String departure = requestParam.getDeparture();
        String arrival = requestParam.getArrival();
        if (!CollUtil.isEmpty(chooseSeats)){
            String trainId = requestParam.getTrainId();
            List<PurchaseTicketPassengerDetailDTO> passengerDetails = requestParam.getPassengers();
            Map<Integer, List<PurchaseTicketPassengerDetailDTO>> TypeToList = passengerDetails.stream().collect(Collectors.groupingBy(PurchaseTicketPassengerDetailDTO::getSeatType));
            String keySuffix = StrUtil.join("-", requestParam.getTrainId(), departure, arrival);
            RedisTemplate instance = cache.getInstance();
            HashOperations<String,Integer,Integer> hashOperations = instance.opsForHash();
            for (Map.Entry<Integer, List<PurchaseTicketPassengerDetailDTO>> entry : TypeToList.entrySet()) {
                Integer type = entry.getKey();
                List<PurchaseTicketPassengerDetailDTO> passengers = entry.getValue();
                String KEY = REMAINTICKETOFSEAT_TRAIN + keySuffix;
                Integer s = hashOperations.get(KEY, type);
                if (s==null){
//                    这里没有保存到座位数量信息,需要查询
//                    根据train_id,seat_type,起点,终点查询座位数量
                    log.info("查询数据库");
                    for (PurchaseTicketPassengerDetailDTO passenger : passengers) {

                        Integer seatType = passenger.getSeatType();
                        Integer seatCount = cache.SafeGetOfHash(KEY, seatType, () -> {
                            return seatMapper.countByTrainIdAndSeatTypeAndArrivalAndDeparture(trainId, seatType, departure, arrival);
                        });
                        log.info("座位信息:::{}---{}",seatType,seatCount);
                        if (seatCount <passengers.size())throw new ClientException("无余票");
                    }
                }
            }
        }
    }

//    public void  GetSeatNumFromDB()

    /**
     * @return
     */
    @Override
    public int getOrder() {
        return 1;
    }
}
