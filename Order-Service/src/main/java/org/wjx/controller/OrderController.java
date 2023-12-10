package org.wjx.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.wjx.Res;
import org.wjx.service.OrderItemService;
import org.wjx.service.OrderService;

/**
 * @author xiu
 * @create 2023-12-07 12:17
 */
@RestController
@RequiredArgsConstructor
public class OrderController {
    final OrderService orderService;

    /**
     * 车票订单关闭
     */
    @PostMapping("/api/order-service/order/ticket/close")
    public Res<Boolean> closeTickOrder(String  orderSn) {
        return Res.success(orderService.cancelOrCloseTickOrder(orderSn));
    }

    /**
     * 车票订单取消
     */
    @PostMapping("/api/order-service/order/ticket/cancel")
    public Res<Boolean> cancelTickOrder( String  orderSn) {
        return Res.success(orderService.cancelOrCloseTickOrder(orderSn));
    }
}
