package org.wjx.dto.req;

import lombok.Data;
import org.wjx.handler.DTO.PurchaseTicketPassengerDetailDTO;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author xiu
 * @create 2023-11-28 15:19
 */
@Data
public class PurchaseTicketReqDTO {
    @NotBlank
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
    @NotBlank
    private String departure;

    /**
     * 到达站点
     */
    @NotBlank
    private String arrival;
}
