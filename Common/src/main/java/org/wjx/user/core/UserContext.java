package org.wjx.user.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.wjx.Exception.ServiceException;

import java.util.Optional;

/**
 * @author xiu
 * @create 2023-11-20 16:34
 */
@Component
@Slf4j
public  class UserContext {
    private static final ThreadLocal<UserInfoDTO> USER_THREAD_LOCAL = new ThreadLocal<>();
    public static void set(UserInfoDTO userInfoDTO){
        if (userInfoDTO==null) throw new ServiceException("用户信息无效");
        USER_THREAD_LOCAL.set(userInfoDTO);
    }
    public static String getUserId(){
        UserInfoDTO userInfoDTO = USER_THREAD_LOCAL.get();
        log.info(Thread.currentThread().getName());
        log.info("用户登录的信息----------------------{}",userInfoDTO);
        return Optional.ofNullable( userInfoDTO).map(UserInfoDTO::getUserId).orElse("");
    }
    public static String getUserName(){
        UserInfoDTO userInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable( userInfoDTO).map(UserInfoDTO::getUsername).orElse("");
    }
    public static String getToken(){
        UserInfoDTO userInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable( userInfoDTO).map(UserInfoDTO::getToken).orElse("");
    }
    public static String getRealName(){
        UserInfoDTO userInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable( userInfoDTO).map(UserInfoDTO::getRealName).orElse("");
    }
    public static void removeUser(){
        USER_THREAD_LOCAL.remove();
    }
}
