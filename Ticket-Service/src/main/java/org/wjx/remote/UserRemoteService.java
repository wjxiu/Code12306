package org.wjx.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.wjx.Res;
import org.wjx.remote.dto.PassengerRespDTO;

import java.util.List;

/**
 * @author xiu
 * @create 2023-12-06 16:39
 */
@FeignClient("user-service")
public interface UserRemoteService {

    @GetMapping("/passenger/inner/passenger/actual/query/ids")
    Res<List<PassengerRespDTO>> listPassengerQueryByIds(@RequestParam("username") String username, @RequestParam("ids") List<String> ids);

}
