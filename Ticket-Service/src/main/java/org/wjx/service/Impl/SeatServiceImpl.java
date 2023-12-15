package org.wjx.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;
import org.wjx.Exception.ServiceException;
import org.wjx.core.SafeCache;
import org.wjx.dao.DO.RouteDTO;
import org.wjx.dao.DO.SeatDO;
import org.wjx.dao.mapper.SeatMapper;
import org.wjx.dto.resp.TrainPurchaseTicketRespDTO;
import org.wjx.enums.SeatStatusEnum;
import org.wjx.service.SeatService;
import org.wjx.service.TrainStationService;

import java.util.List;

import static org.wjx.constant.RedisKeyConstant.REMAINTICKETOFSEAT_TRAIN;

/**
 * @author xiu
 * @create 2023-11-30 9:42
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeatServiceImpl extends ServiceImpl<SeatMapper, SeatDO> implements SeatService {
    final SeatMapper seatMapper;
    final TrainStationService trainStationService;
    final SafeCache cache;

    /**
     * 获取列车车厢中可用座位的座位号集合
     *
     * @param trainId        列车 ID
     * @param carriageNumber 车厢号
     * @param seatType       座位类型
     * @param departure      出发站
     * @param arrival        到达站
     * @return 可用座位的座位号集合
     */
    @Override
    public List<String> listAvailableSeat(String trainId, String carriageNumber, Integer seatType, String departure, String arrival) {
        LambdaQueryWrapper<SeatDO> eq = new QueryWrapper<SeatDO>()
                .select("distinct  seat_number")
                .lambda()
                .eq(SeatDO::getTrainId, trainId)
                .eq(SeatDO::getCarriageNumber, carriageNumber)
                .eq(SeatDO::getSeatType, seatType)
                .eq(SeatDO::getStartStation, departure)
                .eq(SeatDO::getEndStation, arrival)
                .eq(SeatDO::getSeatStatus, SeatStatusEnum.AVAILABLE.getCode());

        List<SeatDO> list = list(eq);
        return list.stream().map(SeatDO::getSeatNumber).toList();
    }

    /**
     * 获取列车车厢余票集合
     *
     * @param trainId           列车 ID
     * @param departure         出发站
     * @param arrival           到达站
     * @param trainCarriageList 车厢编号集合
     * @return 车厢余票的集合
     */
    @Override
    public List<Integer> listSeatRemainingTicket(String trainId, String departure, String arrival, List<String> trainCarriageList) {
        SeatDO build = SeatDO.builder().trainId(Long.valueOf(trainId)).startStation(departure).endStation(arrival).build();
        log.info("build::::{}", build);
        return seatMapper.listSeatRemainingTicket(build, trainCarriageList);
    }

    /**
     * 查询对应参数的车厢号是有余票的
     *
     * @param trainId      列车 ID
     * @param carriageType 车厢类型
     * @param departure    出发站
     * @param arrival      到达站
     * @return 有余票的车厢号集合
     */
    @Override
    public List<String> listAvailableCarriageNumber(String trainId, Integer carriageType, String departure, String arrival) {
//        todo 缓存
        LambdaQueryWrapper<SeatDO> queryWrapper = Wrappers.lambdaQuery(SeatDO.class)
                .eq(SeatDO::getTrainId, trainId)
                .eq(SeatDO::getSeatType, carriageType)
                .eq(SeatDO::getStartStation, departure)
                .eq(SeatDO::getEndStation, arrival)
                .eq(SeatDO::getSeatStatus, SeatStatusEnum.AVAILABLE.getCode())
                .groupBy(SeatDO::getCarriageNumber)
                .select(SeatDO::getCarriageNumber);
        List<SeatDO> seatDOList = list(queryWrapper);
        log.info("seatDOList:::{}", seatDOList);
        return seatDOList.stream().map(SeatDO::getCarriageNumber).toList();
    }

    /**
     * 锁定选中以及沿途车票状态
     * 锁定后对应的座位数量缓存删除
     *
     * @param trainId                     列车 ID
     * @param departure                   出发站
     * @param arrival                     到达站
     * @param trainPurchaseTicketRespList 乘车人以及座位信息
     */
    @Override
    public void lockSeat(String trainId, String departure, String arrival, List<TrainPurchaseTicketRespDTO> trainPurchaseTicketRespList) {
        log.info("trainPurchaseTicketRespList:::{}", trainPurchaseTicketRespList);
        List<RouteDTO> routeDTOS = trainStationService.listTakeoutTrainStationRoute(trainId, departure, arrival);
        for (RouteDTO routeDTO : routeDTOS) {
            for (TrainPurchaseTicketRespDTO ticket : trainPurchaseTicketRespList) {
                log.info("路线：{}to{}", routeDTO.getStartStation(), routeDTO.getEndStation());
//                fixme 这里重复更新
                LambdaUpdateWrapper<SeatDO> updateWrapper = Wrappers.lambdaUpdate(SeatDO.class)
                        .eq(SeatDO::getTrainId, trainId)
                        .eq(SeatDO::getSeatType, ticket.getSeatType())
                        .eq(SeatDO::getStartStation, routeDTO.getStartStation())
                        .eq(SeatDO::getEndStation, routeDTO.getEndStation())
                        .eq(SeatDO::getCarriageNumber, ticket.getCarriageNumber())
                        .eq(SeatDO::getSeatNumber, ticket.getSeatNumber())
                        .eq(SeatDO::getSeatStatus, 0);
                SeatDO updateSeatDO = SeatDO.builder()
                        .seatStatus(SeatStatusEnum.LOCKED.getCode())
                        .build();
                int update = seatMapper.update(updateSeatDO, updateWrapper);
//                更新成功，删除缓存,没有验证
                if (update > 0) {
                    String keyprefix = String.join(trainId, routeDTO.getStartStation(), routeDTO.getEndStation());
                    String key = REMAINTICKETOFSEAT_TRAIN + keyprefix;
                    HashOperations<String, Integer, Integer> hashOperations = cache.getInstance().opsForHash();
                    hashOperations.delete(key, ticket.getSeatType());
                } else {
                    throw new ServiceException("锁票异常");
                }

            }
        }
    }

    /**
     * 检查座位对应的线路是否全部有票
     *
     * @param trainId
     * @param departureStation
     * @param arrivalStation
     * @param carriageNumber
     * @return true返回足够，false不够
     */
    @Override
//    fixme 改为修改全部
    public boolean checkLockSeat(String trainId, String departureStation, String arrivalStation, Integer seatType, String carriageNumber) {
        List<RouteDTO> routeDTOS = trainStationService.listTakeoutTrainStationRoute(trainId, departureStation, arrivalStation);
        for (RouteDTO routeDTO : routeDTOS) {
            String startStation = routeDTO.getStartStation();
            String endStation = routeDTO.getEndStation();
            SeatDO seatDO = new SeatDO();
            seatDO.setSeatStatus(SeatStatusEnum.LOCKED.getCode());
            Long l = seatMapper.selectCount(new LambdaQueryWrapper<SeatDO>().eq(SeatDO::getSeatType, seatType).eq(SeatDO::getTrainId, trainId)
                    .eq(SeatDO::getStartStation, startStation)
                    .eq(SeatDO::getEndStation, endStation)
//                    todo 添加seatnumber
                    .eq(SeatDO::getCarriageNumber, carriageNumber));
            if (l==0)return false;
        }
        return true;
    }

    /**
     * 解锁选中以及沿途车票状态
     *
     * @param trainId   列车 ID
     * @param departure 出发站
     * @param arrival   到达站
     * @param seatTypes 座位类型
     * @return
     */
    @Override
    public Integer unlock(String trainId, String departure, String arrival, Integer seatTypes) {
        List<RouteDTO> routeDTOS = trainStationService.listTakeoutTrainStationRoute(trainId, departure, arrival);
        for (RouteDTO routeDTO : routeDTOS) {
            LambdaUpdateWrapper<SeatDO> updateWrapper = Wrappers.lambdaUpdate(SeatDO.class)
                    .eq(SeatDO::getTrainId, trainId)
                    .eq(SeatDO::getSeatType, seatTypes)
                    .eq(SeatDO::getStartStation, routeDTO.getStartStation())
                    .eq(SeatDO::getEndStation, routeDTO.getEndStation());
            SeatDO updateSeatDO = SeatDO.builder()
                    .seatStatus(SeatStatusEnum.AVAILABLE.getCode())
                    .build();
            return seatMapper.update(updateSeatDO, updateWrapper);

        }
        return seatTypes;
    }
}
