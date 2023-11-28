package org.wjx.FilterChain;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.wjx.Exception.ClientException;
import org.wjx.dto.req.UserRegisterReqDTO;

import java.util.Objects;

import org.wjx.ErrorCode.UserRegisterErrorCodeEnum;

/**
 * @author xiu
 * @create 2023-11-20 19:48
 */
@Component
public class UserRegisterFilter implements UserRegisterAbstractFilter<UserRegisterReqDTO> {

    @Override
    public int getOrder() {
        return 2;
    }
    @Override
    public void handle(UserRegisterReqDTO requestParam) {
        if (!StringUtils.hasLength(requestParam.getUsername())) {
            throw new ClientException(UserRegisterErrorCodeEnum.USER_NAME_NOTNULL);
        }
        if (!StringUtils.hasLength(requestParam.getPassword())) {
            throw new ClientException(UserRegisterErrorCodeEnum.PASSWORD_NOTNULL);
        } else if (!StringUtils.hasLength(requestParam.getPhone())) {
            throw new ClientException(UserRegisterErrorCodeEnum.PHONE_NOTNULL);
        } else if (Objects.isNull(requestParam.getIdType())) {
            throw new ClientException(UserRegisterErrorCodeEnum.ID_TYPE_NOTNULL);
        } else if (!StringUtils.hasLength(requestParam.getIdCard())) {
            throw new ClientException(UserRegisterErrorCodeEnum.ID_CARD_NOTNULL);
        } else if (!StringUtils.hasLength(requestParam.getMail())) {
            throw new ClientException(UserRegisterErrorCodeEnum.MAIL_NOTNULL);
        } else if (!StringUtils.hasLength(requestParam.getRealName())) {
            throw new ClientException(UserRegisterErrorCodeEnum.REAL_NAME_NOTNULL);
        }
    }
}
