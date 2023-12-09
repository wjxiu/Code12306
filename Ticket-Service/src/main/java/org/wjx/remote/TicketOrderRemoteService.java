package org.wjx.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.wjx.Res;
import org.wjx.config.MyFeignClientConfiguration;
import org.wjx.remote.dto.TicketOrderCreateRemoteReqDTO;

/**
 * @author xiu
 * @create 2023-12-06 19:42
 */
@FeignClient(value = "order-service",configuration = MyFeignClientConfiguration.class)
@Import(MyFeignClientConfiguration.class)
public interface TicketOrderRemoteService {
    /**
     * 创建车票订单
     * @param requestParam 创建车票订单请求参数
     * @return 订单号
     */
    @PostMapping("/api/order-service/order/ticket/create")
    Res<String> createTicketOrder(@RequestBody TicketOrderCreateRemoteReqDTO requestParam);
}
