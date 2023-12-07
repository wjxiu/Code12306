package org.wjx.handler.select;

import Strategy.AbstractStrategyChoose;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.wjx.Exception.ServiceException;
import org.wjx.Res;
import org.wjx.dao.DO.TrainStationPriceDO;
import org.wjx.dao.mapper.TrainStationMapper;
import org.wjx.dao.mapper.TrainStationPriceMapper;
import org.wjx.dto.req.PurchaseTicketReqDTO;
import org.wjx.dto.resp.TrainPurchaseTicketRespDTO;
import org.wjx.enums.vehicle.VehicleSeatTypeEnum;
import org.wjx.enums.vehicle.VehicleTypeEnum;
import org.wjx.handler.DTO.PurchaseTicketPassengerDetailDTO;
import org.wjx.handler.DTO.SelectSeatDTO;
import org.wjx.pool.CustomThreadPool;
import org.wjx.remote.UserRemoteService;
import org.wjx.remote.dto.PassengerRespDTO;
import org.wjx.service.SeatService;
import org.wjx.user.core.UserContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * todo 暂时未知
 * 用于
 *
 * @author xiu
 * @create 2023-12-06 13:13
 */
@RequiredArgsConstructor@Component@Slf4j
public class TrainSeatTypeSelector {
    final AbstractStrategyChoose abstractStrategyChoose;
    final UserRemoteService userRemoteService;
    final TrainStationPriceMapper trainStationPriceMapper;
    final SeatService seatService;
    public List<TrainPurchaseTicketRespDTO> select(Integer trainType, PurchaseTicketReqDTO requestParam) {
        String trainId = requestParam.getTrainId();
        List<TrainPurchaseTicketRespDTO> actrualRes=new CopyOnWriteArrayList<>();
        Map<Integer, List<PurchaseTicketPassengerDetailDTO>> seatTypeMap = requestParam.getPassengers().stream().collect(Collectors.groupingBy(PurchaseTicketPassengerDetailDTO::getSeatType));
        List<String> chooseSeats = requestParam.getChooseSeats();
        if (chooseSeats.size()>1){
            List<Future<List<TrainPurchaseTicketRespDTO>>> futureResults = new ArrayList<>();
//            通过线程池调用distributeSeats来进行购票
            ThreadPoolExecutor poolExecutor = CustomThreadPool.poolExecutor;
            for (Map.Entry<Integer, List<PurchaseTicketPassengerDetailDTO>> entry : seatTypeMap.entrySet()) {
                Integer type = entry.getKey();
                List<PurchaseTicketPassengerDetailDTO> list = entry.getValue();
                Future<List<TrainPurchaseTicketRespDTO>> submit = poolExecutor.submit(() -> distributeSeats(trainType, type, requestParam, list));
                futureResults.add(submit);
            }
            futureResults.forEach(future->{
                try {
                    actrualRes.addAll(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    throw new ServiceException("站点余票不足，请尝试更换座位类型或选择其它站点");
                }
            });
        }else{
            seatTypeMap.forEach((seatType,list)->{
                actrualRes.addAll(distributeSeats(trainType, seatType, requestParam, list));
            });
        }
        if (CollUtil.isEmpty(actrualRes)){
            throw new ServiceException("余票不足");
        }
        List<String> list = requestParam.getPassengers().stream().map(a->a.getPassengerId()).toList();
//        远程查询乘车人的信息
        Res<List<PassengerRespDTO>> passengerList = userRemoteService.listPassengerQueryByIds(UserContext.getUserName(), list);
        actrualRes.forEach(each ->{
            PassengerRespDTO passengerRespDTO = passengerList.getData().stream().filter(pass -> Objects.equals(pass.getId(), each.getPassengerId())).findFirst().get();
            each.setIdType(passengerRespDTO.getIdType());
            each.setIdCard(passengerRespDTO.getIdCard());
            each.setPhone(passengerRespDTO.getPhone());
            each.setUserType(passengerRespDTO.getDiscountType());
            each.setIdType(passengerRespDTO.getIdType());
            each.setRealName(passengerRespDTO.getRealName());
            LambdaQueryWrapper<TrainStationPriceDO> eq=new LambdaQueryWrapper<TrainStationPriceDO>()
                    .eq(TrainStationPriceDO::getTrainId,trainId)
                    .eq(TrainStationPriceDO::getSeatType,each.getSeatType())
                    .eq(TrainStationPriceDO::getDeparture,requestParam.getDeparture())
                    .eq(TrainStationPriceDO::getArrival,requestParam.getArrival())
                    .select(TrainStationPriceDO::getPrice);
            TrainStationPriceDO trainStationPriceDO = trainStationPriceMapper.selectOne(eq);
            each.setAmount(trainStationPriceDO.getPrice());
        });
        seatService.lockSeat(requestParam.getTrainId(), requestParam.getDeparture(), requestParam.getArrival(), actrualRes);
        return actrualRes;
    }


    /**
     * 根据参数调用不同的策略
     *来进行购票
     * @param trainType
     * @param seatType
     * @param requestParam
     * @param passengerSeatDetails
     * @return
     */
    private List<TrainPurchaseTicketRespDTO> distributeSeats(Integer trainType,
                                                             Integer seatType,
                                                             PurchaseTicketReqDTO requestParam,
                                                             List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails) {
        String buildingkey= VehicleTypeEnum.findCNameByCode(trainType)+ VehicleSeatTypeEnum.findCNameByCode(seatType);
        log.info("building-----------{}",buildingkey);
        SelectSeatDTO build = SelectSeatDTO.builder()
                .seatType(seatType)
                .requestParam(requestParam)
                .passengerSeatDetails(passengerSeatDetails)
                .build();
        try {
            return abstractStrategyChoose.chooseAndExecuteResp(buildingkey, build);
        }catch (ServiceException e){
            throw new ServiceException("当前车次列车类型暂未适配");
        }
    }
}
