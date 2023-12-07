package org.wjx.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author xiu
 * @create 2023-12-06 19:27
 */
@RequiredArgsConstructor
public enum TicketStatusEnum {

    /**
     * 未支付
     */
    UNPAID(0),

    /**
     * 已支付
     */
    PAID(1),

    /**
     * 已进站
     */
    BOARDED(2),

    /**
     * 改签
     */
    CHANGED(3),

    /**
     * 退票
     */
    REFUNDED(4),

    /**
     * 已取消
     */
    CLOSED(5);

    @Getter
    private final Integer code;
}
