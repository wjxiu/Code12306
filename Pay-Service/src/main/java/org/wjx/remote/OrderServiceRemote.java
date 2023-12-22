package org.wjx.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author xiu
 * @create 2023-12-22 19:06
 */
@FeignClient("order-service")
public  interface OrderServiceRemote {
    @PostMapping("/api/order-service/remote/ticket/changeStatus")
    Boolean changeOrderAndOrderItemStatus( String  orderSn,Integer beforestatus,Integer afterstatus);
}
