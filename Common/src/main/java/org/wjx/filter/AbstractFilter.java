package org.wjx.filter;

import org.springframework.core.Ordered;

/**
 * 定义抽象参数过滤器
 * @param <T> 被过滤的数据类型
 * @author xiu
 * @create 2023-11-20 19:20
 */
public interface AbstractFilter<T> extends Ordered {
    /**
     * 定义过滤逻辑
     * @param reqParam 被过滤的数据
     */
    void handle(T reqParam);

    /**
     * 设置过滤器的名字,相同名字的被认为是同一组过滤器
     * 推荐使用一个接口定义名字,保存在AbstractFilterChainsContext中
     */
    String mark();
}
