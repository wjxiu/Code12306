package org.wjx.template;

import Strategy.AbstractExecuteStrategy;
import Strategy.IPurchase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.HashOperations;
import org.wjx.core.SafeCache;
import org.wjx.dao.DO.RouteDTO;
import org.wjx.dao.mapper.SeatMapper;
import org.wjx.dto.resp.TrainPurchaseTicketRespDTO;
import org.wjx.handler.DTO.SelectSeatDTO;
import org.wjx.service.TrainStationService;
import org.wjx.user.core.ApplicationContextHolder;
import java.util.List;

import static org.wjx.constant.RedisKeyConstant.REMAINTICKETOFSEAT_TRAIN;

/**
 * @author xiu
 * @create 2023-12-05 18:28
 */
@Slf4j@RequiredArgsConstructor
public abstract class AbstractTrainPurchaseTicketTemplate implements IPurchase, CommandLineRunner, AbstractExecuteStrategy<SelectSeatDTO, List<TrainPurchaseTicketRespDTO>> {
    SafeCache cache;
    TrainStationService trainStationService;
    final SeatMapper seatMapper;
    /**
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        cache = ApplicationContextHolder.getBean(SafeCache.class);
        trainStationService = ApplicationContextHolder.getBean(TrainStationService.class);
//        ticketAvailabilityCacheUpdateType = configurableEnvironment.getProperty("ticket.availability.cache-update.type", "");
    }

    protected abstract List<TrainPurchaseTicketRespDTO> selectSeat(SelectSeatDTO dto);

    @Override
    public List<TrainPurchaseTicketRespDTO> executeResp(SelectSeatDTO selectSeatDTO) {
        HashOperations hashOperations = cache.getInstance().opsForHash();
        List<TrainPurchaseTicketRespDTO> trainPurchaseTicketRespDTOS = selectSeat(selectSeatDTO);
        log.info("trainPurchaseTicketRespDTOS:::{}",trainPurchaseTicketRespDTOS);
        trainPurchaseTicketRespDTOS.forEach(a -> {
            String trainId = selectSeatDTO.getRequestParam().getTrainId();
            String departure = selectSeatDTO.getRequestParam().getDeparture();
            String arrival = selectSeatDTO.getRequestParam().getArrival();
            Integer seatType = a.getSeatType();
            List<RouteDTO> routeDTOS = trainStationService.listTakeoutTrainStationRoute(trainId, departure, arrival);
            routeDTOS.forEach(b -> {
                String startStation = b.getStartStation();
                String endStation = b.getEndStation();
//                这里执行会扣除没有数据的站点座位
//                如果有扣除
//                如果没有就先获取再扣除
                String key = REMAINTICKETOFSEAT_TRAIN + String.join("-", trainId, startStation, endStation);
                Integer seatCount = cache.SafeGetOfHash(key, seatType,
                        () -> seatMapper.countByTrainIdAndSeatTypeAndArrivalAndDeparture(trainId, seatType, departure, arrival));
                if (seatCount<selectSeatDTO.getPassengerSeatDetails().size())throw new RuntimeException("票数不足");
                hashOperations.increment(key, seatType, -selectSeatDTO.getPassengerSeatDetails().size());
            });
        });
        return trainPurchaseTicketRespDTOS;
    }

}
