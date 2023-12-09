package org.wjx.param;

import org.wjx.core.IdempotentExecuteHandler;

/**
 * 对于用参数保证幂等性,
 * 可以使用路径+参数+当前用户id的md5值作为分布式锁的key,
 * 发送请求的时候,如果可以加锁,说明请求没有发送过
 * 如果加锁失败了,说明已经请求已经发送过,抛异常
 * @author xiu
 * @create 2023-11-24 19:10
 */
public interface IdempotentParamService extends IdempotentExecuteHandler {

}
