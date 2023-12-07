package org.wjx.handler.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wjx.dto.req.PurchaseTicketReqDTO;

import java.util.List;

/**
 * @author xiu
 * @create 2023-11-30 10:49
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class SelectSeatDTO {
    /**
     * 座位类型
     */
    private Integer seatType;

    /**
     * 座位对应的乘车人集合
     */
    private List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails;

    /**
     * 购票原始入参
     */
    private PurchaseTicketReqDTO requestParam;
}
