package org.wjx.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.wjx.Res;
import org.wjx.remote.dto.UserQueryActualRespDTO;

import javax.validation.constraints.NotEmpty;

/**
 * @author xiu
 * @create 2023-12-07 15:01
 */
@FeignClient("user-service")
public interface UserRemoteService {
    /**
     * 根据用户名查询乘车人列表
     */
    @GetMapping("/api/user-service/actual/query")
    Res<UserQueryActualRespDTO> queryActualUserByUsername(@RequestParam("username") @NotEmpty String username);
}
