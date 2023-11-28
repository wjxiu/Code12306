package org.wjx.service;

import org.wjx.dto.req.CancelTicketOrderReqDTO;
import org.wjx.dto.req.PurchaseTicketReqDTO;
import org.wjx.dto.req.RefundTicketReqDTO;
import org.wjx.dto.req.TicketPageQueryReqDTO;
import org.wjx.dto.resp.PayInfoRespDTO;
import org.wjx.dto.resp.RefundTicketRespDTO;
import org.wjx.dto.resp.TicketPageQueryRespDTO;
import org.wjx.dto.resp.TicketPurchaseRespDTO;

/**
 * @author xiu
 * @create 2023-11-28 15:16
 */
public interface TicketService {
    /**
     * 根据条件分页查询车票
     * @param requestParam 分页查询车票请求参数
     * @return 查询车票返回结果
     */
    TicketPageQueryRespDTO pageListTicketQueryV1(TicketPageQueryReqDTO requestParam);

    /**
     * 购买车票v1
     * @param requestParam 车票购买请求参数
     * @return 订单好
     */
    TicketPurchaseRespDTO purchaseTicketsV1(PurchaseTicketReqDTO requestParam);
    /**
     * 购买车票v2(高性能)
     * @param requestParam 车票购买请求参数
     * @return 订单号
     */
    TicketPurchaseRespDTO purchaseTicketsV2(PurchaseTicketReqDTO requestParam);

    /**
     * 取消车票
     * @param requestParam 车票取消请求参数
     */
    void cancelTicketOrder(CancelTicketOrderReqDTO requestParam);

    /**
     * 查询支付单详情查询
     * @param orderSn 订单号
     * @return 支付单详情查询
     */
    PayInfoRespDTO getPayInfo(String orderSn);

    /**
     * 公共退款接口
     *
     * @param requestParam 退款请求参数
     * @return 退款返回详情
     */
    RefundTicketRespDTO commonTicketRefund(RefundTicketReqDTO requestParam);
}
