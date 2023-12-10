package org.wjx.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
class a{
    @Transactional
    public void show(){
        hhh();
    }
    public void hhh(){

    }
}