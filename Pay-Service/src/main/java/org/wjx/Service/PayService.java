package org.wjx.Service;

import org.wjx.PayInfoRespDTO;

/**
 * @author xiu
 * @create 2023-12-10 19:21
 */
public interface PayService {
    PayInfoRespDTO getPayInfoByOrderSn(String orderSn);

    PayInfoRespDTO getPayInfoByPaySn(String paySn);
}
