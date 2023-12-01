package org.wjx.purchase;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.wjx.Exception.ServiceException;
import org.wjx.dto.req.PurchaseTicketReqDTO;
import org.wjx.dto.resp.TrainPurchaseTicketRespDTO;
import org.wjx.purchase.DTO.PurchaseTicketPassengerDetailDTO;
import org.wjx.purchase.DTO.SelectSeatDTO;
import org.wjx.select.SeatSelection;
import org.wjx.service.SeatService;
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
public class TrainSecondClassPurchaseTicketHandler {
    final SeatService seatService;

    /**
     * 选择座位
     */
    protected List<TrainPurchaseTicketRespDTO> selectSeats(SelectSeatDTO requestParam) {
        PurchaseTicketReqDTO purchaserequestParam = requestParam.getRequestParam();
        String trainId = purchaserequestParam.getTrainId();
        Integer seatType = requestParam.getSeatType();
        String departure = purchaserequestParam.getDeparture();
        String arrival = purchaserequestParam.getArrival();
//        可用的车厢
        List<String> availableCarrage = seatService.listAvailableCarriageNumber(trainId,
                seatType,
                departure,
                arrival);
//       (不太懂这个干嘛的) 对应车厢的可用座位的车票
        List<Integer> seatRemainingTicket = seatService.listSeatRemainingTicket(trainId, departure, arrival, availableCarrage);
        int totalticketcount = seatRemainingTicket.stream().mapToInt(Integer::intValue).sum();
        if (totalticketcount < purchaserequestParam.getPassengers().size()) {
            throw new ServiceException("余票不足");
        }
//            用户没有选择座位
        if (CollUtil.isEmpty(purchaserequestParam.getChooseSeats())) {
            return selectSeatsWithoutChose(requestParam, availableCarrage, seatRemainingTicket);
        }else{
            return selectSeatsWithChose(requestParam,availableCarrage,seatRemainingTicket);
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
                int[] row = PlaneMapOfSeats[j];
                for (String chooseSeat : chooseSeats) {
                    char c = chooseSeat.charAt(0);
                    if (row[c - 'a'] == 0) {
                        choseseats.add(new int[]{j, c - 'a'});
                    } else {
                        //有一个不符合,删掉之前匹配的座位
                        choseseats.clear();
                    }
                }
            }
            int[][] seatsArray = choseseats.toArray(int[][]::new);
            actualSeatsMap.put(carrageNum, seatsArray);
            if (choseseats.size() == passNum) {
                carriagesNumberSeatsMap.put(carrageNum, seatsArray);
                break;
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

    //用户没有选择座位,随机安排
    private List<TrainPurchaseTicketRespDTO> selectSeatsWithoutChose(SelectSeatDTO requestParam,
                                                                     List<String> availableCarrage,
                                                                     List<Integer> trainStationCarriageRemainingTicket) {
        PurchaseTicketReqDTO purchaserequestParam = requestParam.getRequestParam();
        String trainId = purchaserequestParam.getTrainId();
        Integer seatType = requestParam.getSeatType();
        List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails = requestParam.getPassengerSeatDetails();
        String departure = purchaserequestParam.getDeparture();
        String arrival = purchaserequestParam.getArrival();
        int passNum = passengerSeatDetails.size();
        //车厢号->分配的座位
        Map<String, int[][]> carriagesNumberSeatsMap = new HashMap<>();
//        保存着分配后的地图
        Map<String, int[][]> actualSeatsMap = new HashMap<>();
//        保存每个车厢的剩余座位个数
        Map<String, Integer> demotionStockNumMap = new LinkedHashMap<>();
        for (int i = 0; i < availableCarrage.size(); i++) {
            String carragenum = availableCarrage.get(i);
            Integer remainticketeachcarriage = trainStationCarriageRemainingTicket.get(i);
            if (remainticketeachcarriage < passNum) continue;
            List<String> seats = seatService.listAvailableSeat(trainId, carragenum, seatType, departure, arrival);
            int[][] PlaneMapOfSeats = get2DMapOfSeats(seats);
            //选择的座位
            int[][] seletedSeats = SeatSelection.adjacent(PlaneMapOfSeats, passNum);
//            匹配了,退出选择座位过程
            if (seletedSeats != null) {
                break;
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
//            如果每个车厢都同一车厢连续座位匹配失败
            for (Map.Entry<String, Integer> entry : demotionStockNumMap.entrySet()) {
//              尝试  同一个车厢不邻座匹配
                String carriagesNumberBack = entry.getKey();
                int demotionStockNumBack = entry.getValue();
                if (demotionStockNumBack > passNum) {
                    int[][] actualseats = actualSeatsMap.get(carriagesNumberBack);
                    int[][] selectSeat = SeatSelection.nonAdjacent(actualseats, passNum);
                    if (selectSeat.length == passNum) {
                        carriagesNumberSeatsMap.put(carriagesNumberBack, selectSeat);
                        break;
                    }
                }
            }
            if (seats == null) {
                for (Map.Entry<String, Integer> entry : demotionStockNumMap.entrySet()) {
//              尝试  同一个车厢不邻座匹配
                    String carriagesNumberBack = entry.getKey();
                    int demotionStockNumBack = entry.getValue();
                    int[][] actualseats = actualSeatsMap.get(carriagesNumberBack);
                    int[][] selectSeat = SeatSelection.nonAdjacent(actualseats, demotionStockNumBack);
                    carriagesNumberSeatsMap.put(carriagesNumberBack, selectSeat);
                }
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
                    if (ints[0] <= 9) {
                        selectSeats.add("0" + ints[0] + SeatNumberUtil.convert(2, ints[1]));
                    } else {
                        selectSeats.add("" + ints[0] + SeatNumberUtil.convert(2, ints[1]));
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
        int[][] actualSeats = new int[18][5];
        for (int j = 1; j < 19; j++) {
            for (int k = 1; k < 6; k++) {
                // 当前默认按照复兴号商务座排序，后续这里需要按照简单工厂对车类型进行获取 y 轴
                if (j <= 9) {
                    actualSeats[j - 1][k - 1] = listAvailableSeat.contains("0" + j + SeatNumberUtil.convert(2, k)) ? 0 : 1;
                } else {
                    actualSeats[j - 1][k - 1] = listAvailableSeat.contains("" + j + SeatNumberUtil.convert(2, k)) ? 0 : 1;
                }
            }
        }
        return actualSeats;
    }

}