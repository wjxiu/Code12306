package org.wjx.core;

import lombok.Getter;
import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.wjx.annotation.Idempotent;

/**
 * @author xiu
 * @create 2023-11-22 21:38
 */
@Setter@Getter
public class IdempotentParamWrapper {
    public IdempotentParamWrapper(ProceedingJoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    public IdempotentParamWrapper() {
    }

    @Override
    public String toString() {
        return "IdempotentParamWrapper{" +
                "idempotent=" + idempotent +
                ", lockKey='" + lockKey + '\'' +
                '}';
    }

    Idempotent          idempotent;
    ProceedingJoinPoint joinPoint;
    String              lockKey;
}
