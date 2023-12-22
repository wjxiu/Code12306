package org.wjx.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author xiu
 * @create 2023-12-07 12:23
 */
@RequiredArgsConstructor
public enum OrderItemStatusEnum {
    /**
     * 待支付
     */
    PENDING_PAYMENT(0, "待支付"),

    /**
     * 已支付
     */
    ALREADY_PAID(10, "已支付"),

    /**
     * 已进站
     */
    ALREADY_PULL_IN(20, "已进站"),

    /**
     * 已取消
     */
    CLOSED(30, "已取消"),

    /**
     * 已退票
     */
    REFUNDED(40, "已退票"),

    /**
     * 已改签
     */
    RESCHEDULED(50, "已改签");

    @Getter
    private final Integer status;

    @Getter
    private final String statusName;
}
