package org.wjx.controller.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author xiu
 * @create 2023-11-20 13:06
 */
@Component
@FeignClient(name = "pay-service")
public interface payfeign {
    @GetMapping("/abs")
    public int  getaa();
}
