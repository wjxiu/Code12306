package org.wjx.controller;

import cn.hutool.log.Log;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.wjx.MyLog;
import org.wjx.Res;
import org.wjx.dto.req.TicketOrderCreateReqDTO;
import org.wjx.dto.req.TicketOrderPageQueryReqDTO;
import org.wjx.dto.resp.TicketOrderDetailRespDTO;
import org.wjx.dto.req.TicketOrderItemQueryReqDTO;
import org.wjx.dto.resp.TicketOrderDetailSelfRespDTO;
import org.wjx.dto.resp.TicketOrderPassengerDetailRespDTO;
import org.wjx.page.PageRequest;
import org.wjx.page.PageResponse;
import org.wjx.service.OrderItemService;
import org.wjx.service.OrderService;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author xiu
 * @create 2023-12-07 12:37
 */
@RestController@Validated
@RequiredArgsConstructor
@MyLog@Slf4j
public class TicketOrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    /**
     * 根据订单号查询车票订单
     */
    @GetMapping("/api/order-service/order/ticket/query")
    public Res<TicketOrderDetailRespDTO> queryTicketOrderByOrderSn(@NotBlank  String orderSn) {
        return Res.success(orderService.queryTicketOrderByOrderSn(orderSn));
    }
    /**
     * 根据子订单记录id查询车票子订单详情
     */
    @GetMapping("/api/order-service/order/item/ticket/query")
    public Res<List<TicketOrderPassengerDetailRespDTO>> queryTicketItemOrderById(@RequestBody TicketOrderItemQueryReqDTO requestParam) {
        return Res.success(orderItemService.queryTicketItemOrderById(requestParam));
    }
    /**
     * 分页查询车票订单
     */
    @GetMapping("/api/order-service/order/ticket/page")
    public Res<PageResponse<TicketOrderDetailRespDTO>> pageTicketOrder(TicketOrderPageQueryReqDTO requestParam) {
        return Res.success(orderService.pageTicketOrder(requestParam));
    }

    /**
     * 分页查询本人车票订单
     */
    @GetMapping("/api/order-service/order/ticket/self/page")
    public Res<PageResponse<TicketOrderDetailSelfRespDTO>> pageSelfTicketOrder(PageRequest requestParam) {
        return Res.success(orderService.pageSelfTicketOrder(requestParam));
    }
    /**
     * 车票订单创建
     */
    @PostMapping("/api/order-service/order/ticket/create")
    public Res<String> createTicketOrder( @RequestBody TicketOrderCreateReqDTO requestParam) {
        String ticketOrder = orderService.createTicketOrder(requestParam);
        return Res.success(ticketOrder);
    }
}
