package org.wjx.param;


import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.wjx.Exception.ClientException;
import org.wjx.annotation.Idempotent;
import org.wjx.core.AbstractIdempotentExecuteHandler;
import org.wjx.core.IdempotentContext;
import org.wjx.core.IdempotentParamWrapper;
import org.wjx.user.core.UserContext;
import java.util.Arrays;

/**
 * 对于用参数保证幂等性,
 * 可以使用参数的md5值作为分布式锁的key,
 * 发送请求的时候,如果可以加锁,说明请求没有发送过
 * 如果加锁失败了,说明已经请求已经发送过,抛异常
 *
 * @author xiu
 * @create 2023-11-27 14:19
 */
@RequiredArgsConstructor
public class IdempotentParamExeciteHandler extends AbstractIdempotentExecuteHandler implements IdempotentParamService {
    final RedissonClient redissonClient;
    final String LOCK="redis:lock:restAPI";


    @Override
    public IdempotentParamWrapper build(ProceedingJoinPoint joinPoint) {
        String argsmd5 = caclMD5(joinPoint);
        String userId = getCurrentUserId();
        String path = getpath();
        String format = String.format("idempotent:path:%s:currentUserId:%s:md5:%s", path, userId, argsmd5);
        IdempotentParamWrapper wrapper = new IdempotentParamWrapper(joinPoint);
        wrapper.setLockKey(format);
        return wrapper;
    }

    @Override
    public void exceptionProcess() {
        unlock();
    }

    @Override
    public void handle(IdempotentParamWrapper wrapper) {
        String lockKey = wrapper.getLockKey();
        RLock lock = redissonClient.getLock(lockKey);
        if (!lock.tryLock()){
            throw new ClientException(wrapper.getIdempotent().message());
        }
        IdempotentContext.put(LOCK,lock);
    }
    @Override
    public void postExecute() {
        unlock();
    }
    private void unlock(){
        RLock lock=null;
        try {
            lock = (RLock)IdempotentContext.get(LOCK);
        }finally {
            if (lock!=null){
                lock.unlock();
            }
        }
    }

    private String  caclMD5(ProceedingJoinPoint joinPoint) {
        if(joinPoint.getArgs().length==0) throw new IllegalArgumentException("禁止参数为空");
        String string = Arrays.toString(joinPoint.getArgs());
        return  DigestUtils.md5DigestAsHex(string.getBytes());
    }
    private String getCurrentUserId() {
        String userId = UserContext.getUserId();
        if(!StringUtils.hasLength(userId)) return "";
//        if(!StringUtils.hasLength(userId)) throw new ClientException("用户ID获取失败，请登录");
        return userId;
    }
    private String getpath(){
        ServletRequestAttributes requestAttributes =(ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return  requestAttributes.getRequest().getServletPath();
    }
}
