package org.wjx.annotation;

import org.wjx.enums.IdempotentSceneEnum;
import org.wjx.enums.IdempotentTypeEnum;

import java.lang.annotation.*;

/**
 * @author xiu
 * @create 2023-11-22 20:48
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {
    /**
     * 幂等失败的提示消息
     * @return
     */
    String message() default "发送速度过快";

    /**
     * 幂等数据的key
     * @return
     */
    String key() default "";

    IdempotentSceneEnum scene() default IdempotentSceneEnum.RESTFUL;
    IdempotentTypeEnum type() default IdempotentTypeEnum.PARAM;
    /**
     * 设置前缀,防止重复
     * MQ 幂等去重可选设置 {@link IdempotentSceneEnum#MQ} and {@link IdempotentTypeEnum#SPEL} 时生效
     */
    String prefix() default "";
    /**
     * 设置防重令牌 Key 过期时间，单位秒，默认 1 小时，MQ 幂等去重可选设置
     * {@link IdempotentSceneEnum#MQ} and {@link IdempotentTypeEnum#SPEL} 时生效
     */
    long timeout() default 3600L;

}
