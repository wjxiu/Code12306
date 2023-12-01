package org.wjx.dto.req;

import lombok.Data;
import org.wjx.purchase.DTO.PurchaseTicketPassengerDetailDTO;

import java.util.List;

/**
 * @author xiu
 * @create 2023-11-28 15:19
 */
@Data
public class PurchaseTicketReqDTO {
    /**
     * 车次 ID
     */
    private String trainId;

    /**
     * 乘车人
     */
    private List<PurchaseTicketPassengerDetailDTO> passengers;

    /**
     * 选择座位
     */
    private List<String> chooseSeats;

    /**
     * 出发站点
     */
    private String departure;

    /**
     * 到达站点
     */
    private String arrival;
}
