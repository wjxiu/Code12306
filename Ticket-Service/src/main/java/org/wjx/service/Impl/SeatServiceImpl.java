package org.wjx.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wjx.dao.DO.RouteDTO;
import org.wjx.dao.DO.SeatDO;
import org.wjx.dao.DO.TrainStationPriceDO;
import org.wjx.dao.mapper.SeatMapper;
import org.wjx.dto.resp.TrainPurchaseTicketRespDTO;
import org.wjx.enums.SeatStatusEnum;
import org.wjx.service.SeatService;
import org.wjx.service.TrainStationService;

import java.util.List;

/**
 * @author xiu
 * @create 2023-11-30 9:42
 */
@Service
@RequiredArgsConstructor
public class SeatServiceImpl  extends ServiceImpl<SeatMapper, SeatDO> implements SeatService  {
    final SeatMapper seatMapper;
    final TrainStationService trainStationService;
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
        LambdaQueryWrapper<SeatDO> queryWrapper = Wrappers.lambdaQuery(SeatDO.class)
                .eq(SeatDO::getTrainId, trainId)
                .eq(SeatDO::getCarriageNumber, carriageNumber)
                .eq(SeatDO::getSeatType, seatType)
                .eq(SeatDO::getStartStation, departure)
                .eq(SeatDO::getEndStation, arrival)
                .eq(SeatDO::getSeatStatus, SeatStatusEnum.AVAILABLE.getCode())
                .select(SeatDO::getSeatNumber);
        List<SeatDO> list = list(queryWrapper);
      return   list.stream().map(SeatDO::getSeatNumber).toList();
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
       return   seatMapper.listSeatRemainingTicket(build,trainCarriageList);
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
        return seatDOList.stream().map(SeatDO::getCarriageNumber).toList();
    }

    /**
     * 锁定选中以及沿途车票状态
     *
     * @param trainId                     列车 ID
     * @param departure                   出发站
     * @param arrival                     到达站
     * @param trainPurchaseTicketRespList 乘车人以及座位信息
     */
    @Override
    public void lockSeat(String trainId, String departure, String arrival, List<TrainPurchaseTicketRespDTO> trainPurchaseTicketRespList) {
        List<RouteDTO> routeDTOS = trainStationService.listTakeoutTrainStationRoute(trainId, departure, arrival);
        for (RouteDTO routeDTO : routeDTOS) {
            for (TrainPurchaseTicketRespDTO ticket : trainPurchaseTicketRespList) {
                LambdaUpdateWrapper<SeatDO> updateWrapper = Wrappers.lambdaUpdate(SeatDO.class)
                        .eq(SeatDO::getTrainId,trainId)
                        .eq(SeatDO::getSeatType,ticket.getSeatType())
                        .eq(SeatDO::getStartStation,routeDTO.getStartStation())
                        .eq(SeatDO::getEndStation,routeDTO.getEndStation());
                SeatDO updateSeatDO = SeatDO.builder()
                        .seatStatus(SeatStatusEnum.LOCKED.getCode())
                        .build();
                seatMapper.update(updateSeatDO, updateWrapper);
            }
        }
    }

    /**
     * 解锁选中以及沿途车票状态
     *
     * @param trainId                    列车 ID
     * @param departure                  出发站
     * @param arrival                    到达站
     * @param trainPurchaseTicketResults 乘车人以及座位信息
     */
    @Override
    public void unlock(String trainId, String departure, String arrival, List<TrainPurchaseTicketRespDTO> trainPurchaseTicketResults) {
        List<RouteDTO> routeDTOS = trainStationService.listTakeoutTrainStationRoute(trainId, departure, arrival);
        for (RouteDTO routeDTO : routeDTOS) {
            for (TrainPurchaseTicketRespDTO ticket : trainPurchaseTicketResults) {
                LambdaUpdateWrapper<SeatDO> updateWrapper = Wrappers.lambdaUpdate(SeatDO.class)
                        .eq(SeatDO::getTrainId,trainId)
                        .eq(SeatDO::getSeatType,ticket.getSeatType())
                        .eq(SeatDO::getStartStation,routeDTO.getStartStation())
                        .eq(SeatDO::getEndStation,routeDTO.getEndStation());
                SeatDO updateSeatDO = SeatDO.builder()
                        .seatStatus(SeatStatusEnum.AVAILABLE.getCode())
                        .build();
                seatMapper.update(updateSeatDO, updateWrapper);
            }
        }
    }
}
