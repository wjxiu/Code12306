package org.wjx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xiu
 * @create 2023-12-11 10:02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class PayResponse {

    /**
     * 调用支付返回信息
     */
    private String body;
}

