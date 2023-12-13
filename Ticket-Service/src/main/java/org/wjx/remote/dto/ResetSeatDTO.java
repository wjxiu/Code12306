package org.wjx.remote.dto;

import lombok.Data;

/**
 * 取消或关闭订单恢复座位DTO
 * @author xiu
 * @create 2023-12-10 16:17
 */
@Data
public class ResetSeatDTO {
    Long trainId;
    String carriageNumber;
    String seatNumber;
    Integer seatType;
    String startStation;
    String endStation;
}
