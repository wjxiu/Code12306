package org.wjx.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.wjx.MyLog;
import org.wjx.Res;
import org.wjx.dto.req.CancelTicketOrderReqDTO;
import org.wjx.dto.req.PurchaseTicketReqDTO;
import org.wjx.dto.req.RefundTicketReqDTO;
import org.wjx.dto.req.TicketPageQueryReqDTO;
import org.wjx.dto.resp.PayInfoRespDTO;
import org.wjx.dto.resp.RefundTicketRespDTO;
import org.wjx.dto.resp.TicketPageQueryRespDTO;
import org.wjx.dto.resp.TicketPurchaseRespDTO;
import org.wjx.service.TicketService;

/**
 * @author xiu
 * @create 2023-11-28 15:09
 */
@MyLog
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/ticket-service/ticket")
public class TicketController {
    private final TicketService ticketService;
    @GetMapping("/query")
    public Res<TicketPageQueryRespDTO> pageListTicketQuery(TicketPageQueryReqDTO requestParam) {
        return Res.success(ticketService.pageListTicketQueryV1(requestParam));
    }
    @PostMapping("/purchase")
    public Res<TicketPurchaseRespDTO> purchaseTickets(@RequestBody PurchaseTicketReqDTO requestParam) {
        return Res.success(ticketService.purchaseTicketsV1(requestParam));
    }
    @PostMapping("/purchase/v2")
    public Res<TicketPurchaseRespDTO> purchaseTicketsV2(@RequestBody PurchaseTicketReqDTO requestParam) {
        return Res.success(ticketService.purchaseTicketsV2(requestParam));
    }
    @PostMapping("/cancel")
    public Res<Void> cancelTicketOrder(@RequestBody CancelTicketOrderReqDTO requestParam) {
        ticketService.cancelTicketOrder(requestParam);
        return Res.success();
    }
    @GetMapping("/query")
    public Res<PayInfoRespDTO> getPayInfo(@RequestParam(value = "orderSn") String orderSn) {
        return Res.success(ticketService.getPayInfo(orderSn));
    }
    /**
     * 公共退款接口
     */
    @PostMapping("/api/ticket-service/ticket/refund")
    public Res<RefundTicketRespDTO> commonTicketRefund(@RequestBody RefundTicketReqDTO requestParam) {
        return Res.success(ticketService.commonTicketRefund(requestParam));
    }
}
