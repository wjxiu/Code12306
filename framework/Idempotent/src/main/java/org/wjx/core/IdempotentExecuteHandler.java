package org.wjx.core;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.wjx.annotation.Idempotent;

/**
 * 这是说明
 * @author xiu
 * @create 2023-11-24 19:05
 */
public interface IdempotentExecuteHandler {
    /**
     * 如何构建IdempotentParamWrapper
     *  构建内容
     *  Idempotent
     *  ProceedingJoinPoint
     *  String
     * @param joinPoint 参数
     * @return 包装类
     */
    IdempotentParamWrapper build(ProceedingJoinPoint joinPoint);

    /**
     * 保证幂等性的具体逻辑
     * @param wrapper 包装类
     */
    void handle(IdempotentParamWrapper wrapper);

    /**
     * 执行handle方法,
     * @param joinPoint
     * @param idempotent
     */
    void execute(ProceedingJoinPoint joinPoint, Idempotent idempotent);

    /**
     * 异常处理
     */
    default void exceptionProcess() {}

    /**
     * 后置处理
     */
    default void postExecute() {}
}
