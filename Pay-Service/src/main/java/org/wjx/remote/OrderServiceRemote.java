package org.wjx.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;

/**
 * @author xiu
 * @create 2023-12-22 19:06
 */
@FeignClient("order-service")
public  interface OrderServiceRemote {
    @PostMapping("/api/order-service/remote/ticket/changeStatus")
    Boolean changeOrderAndOrderItemStatus( String  orderSn,Integer beforestatus,Integer afterstatus);

    /**
     * 获取订单的出行时间
     * @param orderSn
     * @return
     */
    @PostMapping("/api/order-service/remote/ticket/DepartTime")
    LocalDateTime getDepartTimeByOrderSn(String orderSn);
}
