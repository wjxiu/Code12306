package org.wjx.FilterChain;

import org.springframework.context.annotation.Bean;
import org.wjx.dto.req.UserRegisterReqDTO;
import org.wjx.enums.UserChainMarkEnum;
import org.wjx.filter.AbstractFilter;

/**
 * @author xiu
 * @create 2023-11-20 19:49
 */
public interface UserRegisterAbstractFilter<T> extends AbstractFilter<T> {
    @Override
    default String mark(){
        return UserChainMarkEnum.USER_REGISTER_FILTER.name();
    }
}
