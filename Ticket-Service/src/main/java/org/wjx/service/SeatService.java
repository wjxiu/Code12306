package org.wjx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.wjx.dao.DO.SeatDO;
import org.wjx.dto.resp.TrainPurchaseTicketRespDTO;

import java.util.List;

/**
 * @author xiu
 * @create 2023-11-30 9:42
 */
public interface SeatService extends IService<SeatDO> {
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
    List<String> listAvailableSeat(String trainId, String carriageNumber, Integer seatType, String departure, String arrival);
    /**
     * 获取有余票的车厢个数
     *
     * @param trainId           列车 ID
     * @param departure         出发站
     * @param arrival           到达站
     * @param trainCarriageList 车厢编号集合
     * @return 获取有余票的车厢个数
     */
    List<Integer> listSeatRemainingTicket(String trainId, String departure, String arrival, List<String> trainCarriageList);


    /**
     * 查询列车有余票的车厢号集合
     *
     * @param trainId      列车 ID
     * @param carriageType 车厢类型
     * @param departure    出发站
     * @param arrival      到达站
     * @return 车厢号集合
     */
    List<String> listAvailableCarriageNumber(String trainId, Integer carriageType, String departure, String arrival);

    /**
     * 锁定选中以及沿途车票状态
     *
     * @param trainId                     列车 ID
     * @param departure                   出发站
     * @param arrival                     到达站
     * @param trainPurchaseTicketRespList 乘车人以及座位信息
     */
    void lockSeat(String trainId, String departure, String arrival, List<TrainPurchaseTicketRespDTO> trainPurchaseTicketRespList);

    /**
     * 解锁选中以及沿途车票状态
     *
     * @param trainId                    列车 ID
     * @param departure                  出发站
     * @param arrival                    到达站
     * @param trainPurchaseTicketResults 乘车人以及座位信息
     */
    void unlock(String trainId, String departure, String arrival, List<TrainPurchaseTicketRespDTO> trainPurchaseTicketResults);

}
