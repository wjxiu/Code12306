package org.wjx.spel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.wjx.Exception.ClientException;
import org.wjx.annotation.Idempotent;
import org.wjx.core.AbstractIdempotentExecuteHandler;
import org.wjx.core.IdempotentAspect;
import org.wjx.core.IdempotentContext;
import org.wjx.core.IdempotentParamWrapper;
import org.wjx.utils.SpELUtil;

import java.lang.reflect.Method;

/**
 * @author xiu
 * @create 2023-11-27 15:05
 */
@RequiredArgsConstructor@Slf4j
public class IdempotentSPELExecuteHandler extends AbstractIdempotentExecuteHandler implements IdempotentSPELService {
    final RedissonClient redissonClient;
    static final String LOCK = "Idempotent::SPEL::RESTAPI";

    @Override
    public IdempotentParamWrapper build(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Idempotent idempotent = IdempotentAspect.getIdempotent(joinPoint);
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String lockKey = (String) SpELUtil.parseKey(idempotent.key(), method, args);
        log.info("lockkey{}----------",lockKey);
        IdempotentParamWrapper wrapper = new IdempotentParamWrapper(joinPoint);
        wrapper.setLockKey(lockKey);
        return wrapper;
    }

    @Override
    public void handle(IdempotentParamWrapper wrapper) {
        String lockKey = wrapper.getIdempotent().prefix() + wrapper.getLockKey();
        RLock lock = redissonClient.getLock(lockKey);
        boolean b = lock.tryLock();
        if (!b) {
            throw new ClientException(wrapper.getIdempotent().message());
        }
        IdempotentContext.put(LOCK, lock);
    }

    @Override
    public void postExecute() {
        unlock();
    }
    private void unlock(){
        RLock lock = null;
        try {
            lock = (RLock) IdempotentContext.get(LOCK);
        } finally {
            if (lock!=null){
                lock.unlock();
                log.info("已经解锁了-------------");
            }
        }
    }
    @Override
    public void exceptionProcess() {
       unlock();
    }
}
