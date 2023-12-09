package org.wjx.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.wjx.Exception.ClientException;
import org.wjx.utils.JWTUtil;
import org.wjx.user.core.UserContext;
import org.wjx.user.core.UserInfoDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * @author xiu
 * @create 2023-11-22 18:45
 */
@Component
@Slf4j
@Order(0)
public class TokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String servletPath = request.getServletPath();
        List<String> list = Arrays.asList("/setfortest", "/error", "/getfortest","/createToken","/test");
        boolean inwhitelist = list.stream().anyMatch(servletPath::startsWith);
        if (inwhitelist)return true;
        String token = request.getHeader("Authorization");
        if (!StringUtils.hasLength(token))throw new  ClientException("缺少token");
        UserInfoDTO userInfoDTO = JWTUtil.parseJwtToken(token);
        if (userInfoDTO==null||!StringUtils.hasLength(userInfoDTO.getUserId()))throw new  ClientException("token无效");
        log.info("token解析的线程名字:{}",Thread.currentThread().getName());
        log.info("token解析的用户数据-----------{}",userInfoDTO);
        UserContext.set(userInfoDTO);
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
