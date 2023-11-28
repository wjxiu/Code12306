package org.wjx.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.wjx.Res;
import org.wjx.dto.UserDeletionReqDTO;
import org.wjx.dto.req.UserQueryActualRespDTO;
import org.wjx.dto.req.UserRegisterReqDTO;
import org.wjx.dto.req.UserUpdateReqDTO;
import org.wjx.dto.resp.UserQueryRespDTO;
import org.wjx.dto.resp.UserRegisterRespDTO;
import org.wjx.service.UserLoginService;
import org.wjx.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

/**
 * @author xiu
 * @create 2023-11-21 19:05
 */
@RestController@RequiredArgsConstructor
public class UserInfoController {
    final UserService userService;
    final UserLoginService userLoginService;

    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("/api/user-service/query")
    public Res<UserQueryRespDTO> queryUserByUsername(@RequestParam("username") @NotEmpty String username) {
        return Res.success(userService.queryUserByUsername(username));
    }
    /**
     * 根据用户名查询用户无脱敏信息
     */
    @GetMapping("/api/user-service/actual/query")
    public Res<UserQueryActualRespDTO> queryActualUserByUsername(@RequestParam("username") @NotEmpty String username) {
        return Res.success(userService.queryActualUserByUsername(username));
    }

    /**
     * 检查用户名是否已存在
     */
    @GetMapping("/api/user-service/has-username")
    public Res<Boolean> hasUsername(@RequestParam("username") @NotEmpty String username) {
        return Res.success(userLoginService.haveUserName(username));
    }
    /**
     * 修改用户
     */
    @PostMapping("/api/user-service/update")
    public Res<Void> update(@RequestBody @Valid UserUpdateReqDTO requestParam) {
        userService.update(requestParam);
        return Res.success();
    }

    /**
     * 注销用户
     */
    @PostMapping("/api/user-service/deletion")
    public Res<Void> deletion(@RequestBody @Valid UserDeletionReqDTO requestParam) {
        userLoginService.deletion(requestParam);
        return Res.success();
    }

}
