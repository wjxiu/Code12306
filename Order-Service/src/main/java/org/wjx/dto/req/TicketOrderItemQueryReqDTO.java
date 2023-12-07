package org.wjx.dto.req;

import lombok.Data;

import java.util.List;

/**
 * @author xiu
 * @create 2023-12-07 12:47
 */
@Data
public class TicketOrderItemQueryReqDTO {
    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 子订单记录id
     */
    private List<Long> orderItemRecordIds;
}
