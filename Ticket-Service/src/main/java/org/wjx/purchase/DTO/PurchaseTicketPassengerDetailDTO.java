package org.wjx.purchase.DTO;

import lombok.Data;

/**
 * @author xiu
 * @create 2023-11-30 10:50
 */
@Data
public class PurchaseTicketPassengerDetailDTO {

    /**
     * 乘车人 ID
     */
    private String passengerId;

    /**
     * 座位类型
     */
    private Integer seatType;
}
