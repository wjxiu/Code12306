package org.wjx.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wjx.MyLog;
import org.wjx.Res;
import org.wjx.dto.req.UserLoginReqDTO;
import org.wjx.dto.req.UserRegisterReqDTO;
import org.wjx.dto.resp.UserLoginRespDTO;
import org.wjx.dto.resp.UserRegisterRespDTO;
import org.wjx.service.UserLoginService;

/**
 * @author xiu
 * @create 2023-11-20 15:08
 */
@RequestMapping("/api/user-service")
@RestController@MyLog
@RequiredArgsConstructor
public class UserLoginController {
   final UserLoginService userLoginService;
    @PostMapping("/v1/login")
    public Res<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParam) {
        return Res.success(userLoginService.login(requestParam));
    }
    @PostMapping("/v1/register")
    public Res<UserRegisterRespDTO> register(@RequestBody UserRegisterReqDTO requestParam) {
        return Res.success(userLoginService.register(requestParam));
    }

}
