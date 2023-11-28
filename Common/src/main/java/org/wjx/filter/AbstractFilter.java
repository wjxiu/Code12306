package org.wjx.filter;

import org.springframework.core.Ordered;

/**
 * @author xiu
 * @create 2023-11-20 19:20
 */
public interface AbstractFilter<T> extends Ordered {
    void handle(T reqParam);

    String mark();
}
