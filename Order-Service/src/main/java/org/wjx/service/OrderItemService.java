package org.wjx.service;

import org.wjx.dto.normal.OrderItemStatusReversalDTO;
import org.wjx.dto.req.TicketOrderItemQueryReqDTO;
import org.wjx.dto.resp.TicketOrderPassengerDetailRespDTO;

import java.util.List;

/**
 * @author xiu
 * @create 2023-12-07 12:47
 */
public interface OrderItemService {
    /**
     * 根据子订单记录id查询车票子订单详情
     */
    List<TicketOrderPassengerDetailRespDTO> queryTicketItemOrderById(TicketOrderItemQueryReqDTO requestParam);

    /**
     * 子订单状态反转
     *
     * @param requestParam 请求参数
     */
    void orderItemStatusReversal(OrderItemStatusReversalDTO requestParam);


}
