package org.wjx.handler;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.wjx.Exception.ServiceException;
import org.wjx.dao.DO.SeatDO;
import org.wjx.dao.mapper.SeatMapper;
import org.wjx.dto.req.PurchaseTicketReqDTO;
import org.wjx.dto.resp.TrainPurchaseTicketRespDTO;
import org.wjx.enums.SeatStatusEnum;
import org.wjx.enums.vehicle.VehicleSeatTypeEnum;
import org.wjx.enums.vehicle.VehicleTypeEnum;
import org.wjx.handler.DTO.PurchaseTicketPassengerDetailDTO;
import org.wjx.handler.DTO.SelectSeatDTO;
import org.wjx.handler.select.SeatSelection;
import org.wjx.service.SeatService;
import org.wjx.template.AbstractTrainPurchaseTicketTemplate;
import org.wjx.utils.SeatNumberUtil;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * 负责二等座的购票
 *
 * @author xiu
 * @create 2023-11-30 10:48
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TrainSecondClassPurchaseTicketHandler extends AbstractTrainPurchaseTicketTemplate {
    final SeatService seatService;
    final SeatMapper seatMapper;

    @Override
    public String mark() {
        return VehicleTypeEnum.HIGH_SPEED_RAIN.getName() + VehicleSeatTypeEnum.SECOND_CLASS.getName();
    }

    /**
     * 选择座位
     */
    @Override
    protected List<TrainPurchaseTicketRespDTO> selectSeat(SelectSeatDTO requestParam) {
        PurchaseTicketReqDTO purchaserequestParam = requestParam.getRequestParam();
        String trainId = purchaserequestParam.getTrainId();
        Integer seatType = requestParam.getSeatType();
        String departure = purchaserequestParam.getDeparture();
        String arrival = purchaserequestParam.getArrival();
        log.info("requestParam:::{}", requestParam);
//        可用的车厢
        List<String> availableCarrage = seatService.listAvailableCarriageNumber(trainId,
                seatType,
                departure,
                arrival);
//     对应车厢的可用座位的车票
        log.info("availableCarrage:::::{}", availableCarrage);
        List<Integer> seatRemainingTicket = seatService.listSeatRemainingTicket(trainId, departure, arrival, availableCarrage);
        int totalticketcount = seatRemainingTicket.stream().mapToInt(Integer::intValue).sum();
        if (totalticketcount < purchaserequestParam.getPassengers().size()) {
            throw new ServiceException("余票不足");
        }
//            用户没有选择座位
        if (CollUtil.isEmpty(purchaserequestParam.getChooseSeats())) {
            return selectSeatsWithoutChose(requestParam, availableCarrage, seatRemainingTicket);
        } else {
            return selectSeatsWithChose(requestParam, availableCarrage, seatRemainingTicket);
        }
    }

    /**
     * 根据用户选择的座位安排,如果没有符合的,想尝试安排连在一起的,如果连在一起的座位都没有,安排不连在一起的
     *
     * @param requestParam
     * @param availableCarrage
     * @param trainStationCarriageRemainingTicket
     * @return
     */
    private List<TrainPurchaseTicketRespDTO> selectSeatsWithChose(SelectSeatDTO requestParam,
                                                                  List<String> availableCarrage,
                                                                  List<Integer> trainStationCarriageRemainingTicket) {
        PurchaseTicketReqDTO purchaserequestParam = requestParam.getRequestParam();
        String trainId = purchaserequestParam.getTrainId();
        Integer seatType = requestParam.getSeatType();
        List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails = requestParam.getPassengerSeatDetails();
        String departure = purchaserequestParam.getDeparture();
        List<String> chooseSeats = purchaserequestParam.getChooseSeats();
        String arrival = purchaserequestParam.getArrival();
        int passNum = passengerSeatDetails.size();
        //车厢号->分配的座位
        Map<String, int[][]> carriagesNumberSeatsMap = new HashMap<>();
//        保存着分配后的地图
        Map<String, int[][]> actualSeatsMap = new HashMap<>();
//        保存每个车厢的剩余座位个数
        Map<String, Integer> demotionStockNumMap = new LinkedHashMap<>();
        for (int i = 0; i < availableCarrage.size(); i++) {
            Integer remainticket = trainStationCarriageRemainingTicket.get(i);
            if (remainticket < passNum) continue;
            String carrageNum = availableCarrage.get(i);
            List<String> availableseats = seatService.listAvailableSeat(trainId, carrageNum, seatType, departure, arrival);
            int[][] PlaneMapOfSeats = get2DMapOfSeats(availableseats);
            ArrayList<int[]> choseseats = new ArrayList<>();
            //            车厢的每一行都匹配一次
            for (int j = 0; j < PlaneMapOfSeats.length; j++) {
                if (choseseats.size()==passNum)break;
                int[] row = PlaneMapOfSeats[j];
                for (String chooseSeat : chooseSeats) {
                    if (choseseats.size()==passNum)break;
                    Character c = Character.toLowerCase(chooseSeat.charAt(0));
                    if (row[Character.toLowerCase(c) - 'a'] == 0) {
                        choseseats.add(new int[]{j, c - 'a'});
                    } else {
                        //有一个不符合,删掉之前匹配的座位
                    if (!choseseats.isEmpty())choseseats.clear();
                    }
                }
            }
            int[][] seatsArray = choseseats.toArray(int[][]::new);
            actualSeatsMap.put(carrageNum, seatsArray);
            if (choseseats.size() == passNum) {
                carriagesNumberSeatsMap.put(carrageNum, seatsArray);
//                todo 检查座位满足要求
                if (seatService.checkLockSeat(trainId, departure, arrival, seatType, carrageNum)) {
                    break;
                }else{
                    continue;
                }
            }
//            算出每个车厢的剩余座位数
            int count = (int) choseseats.stream().flatMapToInt(Arrays::stream).filter(a -> a == 0).count();
            demotionStockNumMap.put(carrageNum, count);
//            如果车厢没有完全匹配,跳到下一个车厢重新开始完全匹配
            if (i < availableseats.size() - 1) continue;
//            都没有
            Set<Map.Entry<String, Integer>> entries = demotionStockNumMap.entrySet();
            for (Map.Entry<String, Integer> entry : entries) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                if (value < passNum) continue;
                int[][] actualSeatOfCarrage = actualSeatsMap.get(key);
                int[][] actrualSeats = null;
                if ((actrualSeats = SeatSelection.adjacent(actualSeatOfCarrage, passNum)) != null) {
                    carriagesNumberSeatsMap.put(key, actrualSeats);
                } else {
                    actrualSeats = SeatSelection.nonAdjacent(actualSeatOfCarrage, passNum);
                    if (actrualSeats.length > 0) {
                        carriagesNumberSeatsMap.put(key, actrualSeats);
                    }
                }
                if (actrualSeats.length == passNum) break;
            }
        }
        return getTrainPurchaseTicketRespDTOS(passengerSeatDetails, carriagesNumberSeatsMap);
    }

    /**
     * 为没有选择座位的用户分配座位，
     * 三级尝试
     * 1. 尝试全部人分配同一车厢相邻座位
     * 2. 尝试分配全部人在同一个车厢
     * 3. 尝试分配不同车厢不相邻
     * 4.如果这个座位不符合要求
     * @param requestParam
     * @param availableCarrage
     * @param trainStationCarriageRemainingTicket
     * @return
     */
    private List<TrainPurchaseTicketRespDTO> selectSeatsWithoutChose(SelectSeatDTO requestParam,
                                                                     List<String> availableCarrage,
                                                                     List<Integer> trainStationCarriageRemainingTicket) {
        PurchaseTicketReqDTO purchaserequestParam = requestParam.getRequestParam();
        String trainId = purchaserequestParam.getTrainId();
        Integer seatType = requestParam.getSeatType();
        List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails = requestParam.getPassengerSeatDetails();
        String departureStation = purchaserequestParam.getDeparture();
        String arrivalStation = purchaserequestParam.getArrival();
        int passNum = passengerSeatDetails.size();
        //车厢号->分配的座位
        Map<String, int[][]> carriagesNumberSeatsMap = new HashMap<>();
//        保存着分配后的地图
        Map<String, int[][]> actualSeatsMap = new HashMap<>();
//        保存每个车厢的剩余座位个数,key->carriagenum,value->对应车厢座位数目
        Map<String, Integer> demotionStockNumMap = new LinkedHashMap<>();
        for (int i = 0; i < availableCarrage.size(); i++) {
            //todo 添加缓存
            String carragenum = availableCarrage.get(i);
            Integer remainticketeachcarriage = trainStationCarriageRemainingTicket.get(i);
            if (remainticketeachcarriage < passNum) continue;
            //fixme 查出一个有用的之后需要将涉及到的线路的票，座位改变状态
            List<String> seats = seatService.listAvailableSeat(trainId, carragenum, seatType, departureStation, arrivalStation);
            int[][] PlaneMapOfSeats = get2DMapOfSeats(seats);
            log.info("二维地图:::{}", PlaneMapOfSeats);
            //选择的座位，第一级尝试分配相邻座位
            int[][] seletedSeats = SeatSelection.adjacent(PlaneMapOfSeats, passNum);
            log.info("选择的座位::{}", seletedSeats);
//            匹配了,退出选择座位过程
            if (seletedSeats != null && seletedSeats.length == passNum) {
                carriagesNumberSeatsMap.put(carragenum, seletedSeats);
                if (seatService.checkLockSeat(trainId,departureStation,arrivalStation,seatType,carragenum)) {
                    break;
                }else{
                    continue;
                }
            }
//            如果当前车厢没有匹配的话,保存 车厢号->分配的座位
            carriagesNumberSeatsMap.put(carragenum, seletedSeats);
            int demotionStockNum = (int) Arrays.stream(seletedSeats).flatMapToInt(Arrays::stream).filter(item -> item == 0).count();
            demotionStockNumMap.putIfAbsent(carragenum, demotionStockNum);
            actualSeatsMap.putIfAbsent(carragenum, seletedSeats);
//            对每个车厢尝试使用同一车厢连续座位匹配
            if (i < availableCarrage.size() - 1) {
                continue;
            }
//           如果每个车厢都同一车厢连续座位匹配失败
//          第二级：尝试同一个车厢不同位置
            for (Map.Entry<String, Integer> entry : demotionStockNumMap.entrySet()) {
                String carriageNum = entry.getKey();
                Integer remianSeatNum = entry.getValue();
//                跳过座位数小于人数的车厢
                if (remianSeatNum > passNum) continue;
                int[][] map = actualSeatsMap.get(carriageNum);
                int[][] selectSeat = SeatSelection.nonAdjacent(map, passNum);
                if (selectSeat.length == passNum) {
                    if (seatService.checkLockSeat(trainId,departureStation,arrivalStation,seatType,carragenum)) {
                        carriagesNumberSeatsMap.put(carragenum, seletedSeats);
                        break;
                    }
                }
            }
//         第二级成功，退出循环
            if (!carriagesNumberSeatsMap.isEmpty()) break;
//            第三级匹配：不同车厢不同座
            for (Map.Entry<String, Integer> entry : demotionStockNumMap.entrySet()) {
                String carriageNum = entry.getKey();
                int remianSeatNum = entry.getValue();
                if (remianSeatNum > passNum) continue;
                int[][] actualseats = actualSeatsMap.get(carriageNum);
                int[][] selectSeat = SeatSelection.nonAdjacent(actualseats, passNum);
                carriagesNumberSeatsMap.put(carriageNum, selectSeat);
            }
        }
        return getTrainPurchaseTicketRespDTOS(passengerSeatDetails, carriagesNumberSeatsMap);
    }

    private List<TrainPurchaseTicketRespDTO> getTrainPurchaseTicketRespDTOS(List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails,
                                                                            Map<String, int[][]> carriagesNumberSeatsMap) {
        int count = (int) carriagesNumberSeatsMap.values().stream().flatMap(Arrays::stream).count();
        List<TrainPurchaseTicketRespDTO> actualResult = new ArrayList<>();
        Set<Map.Entry<String, int[][]>> entries = carriagesNumberSeatsMap.entrySet();
        if (count == passengerSeatDetails.size()) {
            int countNum = 0;
            for (Map.Entry<String, int[][]> entry : entries) {
//               给每个车厢被选中的座位生成01A这样的代号
                List<String> selectSeats = new ArrayList<>();
                for (int[] ints : entry.getValue()) {
                    if (ints[0] < 9) {
                        selectSeats.add("0" + (ints[0] + 1 + SeatNumberUtil.convert(2, ints[1] + 1)));
                    } else {
                        selectSeats.add(ints[0] + 1 + SeatNumberUtil.convert(2, ints[1] + 1));
                    }
                }
//                按照顺序分配,没有座位要求
                for (String selectSeat : selectSeats) {
                    TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                    PurchaseTicketPassengerDetailDTO currentTicketPassenger = passengerSeatDetails.get(countNum++);
                    result.setSeatNumber(selectSeat);
                    result.setSeatType(currentTicketPassenger.getSeatType());
                    result.setCarriageNumber(entry.getKey());
                    result.setPassengerId(currentTicketPassenger.getPassengerId());
                    actualResult.add(result);
                }
            }
        }
        return actualResult;
    }
    @NotNull
    private static int[][] get2DMapOfSeats(List<String> listAvailableSeat) {
        log.info("listAvailableSeat:{}",listAvailableSeat);
        int[][] actualSeats = new int[18][5];
        for (int j = 1; j < 19; j++) {
            for (int k = 1; k < 6; k++) {
                // 当前默认按照复兴号商务座排序，后续这里需要按照简单工厂对车类型进行获取 y 轴
                if (j <= 9) {
                    actualSeats[j - 1][k - 1] = listAvailableSeat.contains("0" + j + SeatNumberUtil.convert(2, k)) ? 0 : 1;
                } else {
                    actualSeats[j - 1][k - 1] = listAvailableSeat.contains( j + SeatNumberUtil.convert(2, k)) ? 0 : 1;
                }
            }
        }
        log.info("地图形状：{}",Arrays.deepToString(actualSeats));
        return actualSeats;
    }


}
