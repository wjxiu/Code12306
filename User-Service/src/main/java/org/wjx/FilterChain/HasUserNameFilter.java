package org.wjx.FilterChain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.wjx.Exception.ServiceException;
import org.wjx.dto.req.UserRegisterReqDTO;
import org.wjx.service.UserLoginService;

/**
 * @author xiu
 * @create 2023-11-20 20:28
 */
@Component
@RequiredArgsConstructor
public class HasUserNameFilter implements UserRegisterAbstractFilter<UserRegisterReqDTO>{
    final UserLoginService loginService;
    @Override
    public void handle(UserRegisterReqDTO reqParam) {
        boolean contain = loginService.haveUserName(reqParam.getUsername());
        if (contain)throw new ServiceException("用户名已存在");
    }

    @Override
    public int getOrder() {
        return 66;
    }

}