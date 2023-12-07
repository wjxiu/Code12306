package org.wjx.handler.DTO;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author xiu
 * @create 2023-11-30 10:50
 */
@Data
public class PurchaseTicketPassengerDetailDTO {

    /**
     * 乘车人 ID
     */
    @NotBlank
    private String passengerId;

    /**
     * 座位类型
     */
    @NotBlank
    private Integer seatType;
}
