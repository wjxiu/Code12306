package org.wjx.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.wjx.Res;
import org.wjx.Service.PayService;
import org.wjx.dto.PayCommand;
import org.wjx.dto.PayRespDTO;

/**
 * @author xiu
 * @create 2023-12-22 18:41
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
        PayRespDTO payRespDTO = payService.commonPay(requestParam);
        return Res.success(payRespDTO);
    }
}
