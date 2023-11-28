package org.wjx.token;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.wjx.ErrorCode.BaseErrorCode;
import org.wjx.Exception.ClientException;
import org.wjx.annotation.Idempotent;
import org.wjx.config.IdempotentProperties;
import org.wjx.core.AbstractIdempotentExecuteHandler;
import org.wjx.core.IdempotentContext;
import org.wjx.core.IdempotentParamWrapper;
import org.wjx.core.SafeCache;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

/**
 * token保证幂等性的的方法是,先申请token,请求带着token来发送请求,
 * 如果token还存在着,说明此时请求可以生效
 * 如果token不存在,说明之前已经有请求发送了,这个请求可以丢弃了
 * @author xiu
 * @create 2023-11-24 19:45
 */
@Component
@RequiredArgsConstructor
public class IdempotentTokenExecuteHandler extends AbstractIdempotentExecuteHandler implements IdempotentTokenService{
    final SafeCache cache;
    final IdempotentProperties idempotentProperties;
    private static final String TOKEN_PREFIX_KEY = "idempotent:token:";
    private static final long TOKEN_EXPIRED_TIME = 9999999999L;
    @Override
    public IdempotentParamWrapper build(ProceedingJoinPoint joinPoint) {
        return new IdempotentParamWrapper(joinPoint);
    }
    public String createToken(){
        String prefix=StringUtils.hasLength(idempotentProperties.getKeyPrefix())?idempotentProperties.getKeyPrefix():TOKEN_PREFIX_KEY;
        Long timeout = Optional.ofNullable(idempotentProperties.getTimeout()).orElse(TOKEN_EXPIRED_TIME);
        String token=prefix+ UUID.randomUUID();
        cache.put(token,"",timeout);
        return token;
    }
    /**
     * 保证幂等性的具体逻辑
     * @param wrapper 包装类
     */
    @Override
    public void handle(IdempotentParamWrapper wrapper) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String token = request.getHeader("token");
        if (!StringUtils.hasLength(token)){
            token=request.getParameter("token");
            if (!StringUtils.hasLength(token)){
                throw new ClientException(BaseErrorCode.IDEMPOTENT_TOKEN_NULL_ERROR);
            }
        }
        Boolean delete = cache.delete(token);
        if (!delete){
            String errmes=StringUtils.hasLength(wrapper.getIdempotent().message())
                    ?wrapper.getIdempotent().message():BaseErrorCode.IDEMPOTENT_TOKEN_DELETE_ERROR.message();
            throw new ClientException(errmes, BaseErrorCode.IDEMPOTENT_TOKEN_DELETE_ERROR);
        }
    }
}
