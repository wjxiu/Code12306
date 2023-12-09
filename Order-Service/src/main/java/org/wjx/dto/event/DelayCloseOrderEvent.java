package org.wjx.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wjx.dto.req.TicketOrderItemCreateReqDTO;

import java.util.List;

/**
 * @author xiu
 * @create 2023-12-07 20:06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DelayCloseOrderEvent {

    /**
     * 车次 ID
     */
    private String trainId;

    /**
     * 出发站点
     */
    private String departure;

    /**
     * 到达站点
     */
    private String arrival;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 乘车人购票信息
     */
    private List<TicketOrderItemCreateReqDTO> trainPurchaseTicketResults;
}

