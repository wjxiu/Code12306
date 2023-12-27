package org.wjx.service;

import org.wjx.dto.req.TicketOrderCreateReqDTO;
import org.wjx.dto.req.TicketOrderPageQueryReqDTO;
import org.wjx.dto.resp.TicketOrderDetailRespDTO;
import org.wjx.dto.resp.TicketOrderDetailSelfRespDTO;
import org.wjx.page.PageRequest;
import org.wjx.page.PageResponse;

import java.time.LocalDateTime;

/**
 * @author xiu
 * @create 2023-12-07 12:18
 */
public interface OrderService {

    /**
     * 跟据订单号查询车票订单
     *
     * @param orderSn 订单号
     * @return 订单详情
     */
    TicketOrderDetailRespDTO queryTicketOrderByOrderSn(String orderSn);

    PageResponse<TicketOrderDetailRespDTO> pageTicketOrder(TicketOrderPageQueryReqDTO requestParam);

    PageResponse<TicketOrderDetailSelfRespDTO> pageSelfTicketOrder(PageRequest requestParam);

    /**
     * 创建火车票订单
     * @param requestParam
     * @return 订单号
     */
    String createTicketOrder(TicketOrderCreateReqDTO requestParam);

    /**
     * 取消订单
     * 更新订单号对应的订单和订单项的状态为取消(30)
     *
     * @param orderSn 订单号
     * @return 全部更新成功返回true, 否则返回false
     */
    public boolean cancelOrCloseTickOrder(String orderSn);

    Boolean changeOrderAndOrderItemStatus(String orderSn,Integer beforestatus,Integer afterstatus);
    /**
     * 获取订单的出行时间
     * @param orderSn
     * @return
     */
    LocalDateTime getDepartTimeByOrderSn(String orderSn);
}
