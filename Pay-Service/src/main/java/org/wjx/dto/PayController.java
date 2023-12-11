package org.wjx.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.wjx.PayInfoRespDTO;
import org.wjx.Res;
import org.wjx.Service.PayService;

/**
 * @author xiu
 * @create 2023-12-10 19:18
 */
@RestController
@RequiredArgsConstructor
public class PayController {
    final PayService payService;


    /**
     * 公共支付接口
     * 对接常用支付方式，比如：支付宝、微信以及银行卡等
     */
    @PostMapping("/api/pay-service/pay/create")
    public Res<PayRespDTO> pay(@RequestBody PayCommand requestParam) {
        PayRequest payRequest = PayRequestConvert.command2PayRequest(requestParam);
        PayRespDTO result = payService.commonPay(payRequest);
        return Res.success(result);
    }


    /**
     * 跟据订单号查询支付单详情
     */
    @GetMapping("/api/pay-service/pay/query/order-sn")
    public Res<PayInfoRespDTO> getPayInfoByOrderSn(@RequestParam(value = "orderSn") String orderSn) {
        return Res.success(payService.getPayInfoByOrderSn(orderSn));
    }

    /**
     * 跟据支付流水号查询支付单详情
     */

    @GetMapping("/api/pay-service/pay/query/pay-sn")
    public Res<PayInfoRespDTO> getPayInfoByPaySn(@RequestParam(value = "paySn") String paySn) {
        return Res.success(payService.getPayInfoByPaySn(paySn));
    }
}