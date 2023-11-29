package org.wjx.common;

/**
 * 车票相关的职责链名字枚举
 * @author xiu
 * @create 2023-11-28 15:54
 */
public enum TicketChainMarkEnum {

    /**
     * 车票查询过滤器
     */
    TRAIN_QUERY_FILTER,

    /**
     * 车票购买过滤器
     */
    TRAIN_PURCHASE_TICKET_FILTER,

    /**
     * 车票退款过滤器
     */
    TRAIN_REFUND_TICKET_FILTER;
}
