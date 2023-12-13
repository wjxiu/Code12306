package org.wjx.remote.dto;

import lombok.Data;

import java.util.List;

/**
 * 取消或关闭订单恢复座位DTO,并且设置订单和叮当项状态
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
