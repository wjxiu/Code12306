package org.wjx.Service;

import org.wjx.PayInfoRespDTO;
import org.wjx.dto.PayRequest;
import org.wjx.dto.PayRespDTO;

/**
 * @author xiu
 * @create 2023-12-10 19:21
 */
public interface PayService {
    PayInfoRespDTO getPayInfoByOrderSn(String orderSn);

    PayInfoRespDTO getPayInfoByPaySn(String paySn);

    PayRespDTO commonPay(PayRequest payRequest);

    PayRespDTO ToAliPay(PayRequest payRequest);
}
