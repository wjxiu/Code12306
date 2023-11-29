package org.wjx.base;

import lombok.Data;

/**
 * @author xiu
 * @create 2023-11-28 15:49
 */
@Data
public class PageRequest {
    /**
     * 当前页
     */
    private Long current = 1L;

    /**
     * 每页显示条数
     */
    private Long size = 10L;
}
