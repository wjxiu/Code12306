package org.wjx.handler;

import Strategy.AbstractExecuteStrategy;
import com.alipay.api.AlipayApiException;
import org.wjx.dto.PayRequest;
import org.wjx.dto.PayResponse;

/**
 * @author xiu
 * @create 2023-12-11 10:00
 */
public  abstract class AbstractPayHandler implements AbstractExecuteStrategy<PayRequest, PayResponse> {

    /**
     * 通用的pay方法
     * @param payRequest
     * @return
     */
  public abstract PayResponse pay(PayRequest payRequest) throws AlipayApiException;
}
