package org.wjx.handler;

import Strategy.AbstractExecuteStrategy;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradeCreateRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradeCreateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.wjx.dto.AliPayRequest;
import org.wjx.dto.PayRequest;
import org.wjx.dto.PayResponse;
import org.wjx.enums.PayChannelEnum;
import org.wjx.enums.PayTradeTypeEnum;

import java.math.BigDecimal;

import static org.wjx.handler.AliPayConf.BUYER_ID;

/**
 * @author xiu
 * @create 2023-12-11 10:01
 */
@Component@Slf4j
public class AliPay extends AbstractPayHandler implements AbstractExecuteStrategy<PayRequest, PayResponse> {
    @Override
    public String mark() {
        return PayChannelEnum.ALI_PAY.getName();
    }

    /**
     * @param payRequest
     * @return
     */
    @Override
    public PayResponse pay(PayRequest payRequest) throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi-sandbox.dl.alipaydev.com/gateway.do",AliPayConf.appid,
                AliPayConf.privateKey,"json","UTF-8",AliPayConf.publicKey,"RSA2");
        AlipayTradeCreateRequest request = new AlipayTradeCreateRequest();
        request.setNotifyUrl(AliPayConf.notify_url);
        request.setReturnUrl(AliPayConf.return_url);
        AliPayRequest aliPayRequest = payRequest.getAliPayRequest();
        System.out.println("-------------------------------------------------------------------------------------");
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(aliPayRequest.getOutOrderSn());
        model.setTotalAmount(String.valueOf(aliPayRequest.getTotalAmount()));
        model.setSubject(aliPayRequest.getSubject());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        request.setBizModel(model);
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", aliPayRequest.getTradeNo());
        bizContent.put("total_amount", aliPayRequest.getTotalAmount().toString());
        bizContent.put("subject", aliPayRequest.getSubject());
        bizContent.put("buyer_id", BUYER_ID);
        bizContent.put("timeout_express", "10m");
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        System.out.println(request.getBizContent());
        request.setBizContent(bizContent.toString());
        AlipayTradeCreateResponse response = alipayClient.execute(request);
        if(response.isSuccess()){
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
        return new PayResponse(response.getBody());
    }

    @Override
    public PayResponse executeResp(PayRequest payRequest) {
        PayResponse res=null;
        try {
          res=  pay(payRequest);
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    public static void main(String[] args) {
        AliPay aliPay = new AliPay();
        AliPayRequest aliPayRequest = new AliPayRequest();
        aliPayRequest.setOutOrderSn("qqqqqq");
        aliPayRequest.setSubject("subject");
        aliPayRequest.setTotalAmount(new BigDecimal(9999999));
        aliPayRequest.setTradeNo("qweqweweq1qweewq");
        PayResponse payResponse = aliPay.executeResp(aliPayRequest);
        System.out.println(payResponse.getBody());
    }
}
