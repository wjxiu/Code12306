package org.wjx.Service.Impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wjx.PayInfoRespDTO;
import org.wjx.Service.PayService;
import org.wjx.dao.DO.PayDO;
import org.wjx.dao.mapper.PayMapper;
import org.wjx.dto.PayRequest;
import org.wjx.dto.PayRespDTO;
import org.wjx.utils.BeanUtil;

/**
 * @author xiu
 * @create 2023-12-10 19:21
 */
@RequiredArgsConstructor
@Service
public class PayServiceImpl implements PayService {
    final PayMapper payMapper;
    @Override
    public PayInfoRespDTO getPayInfoByOrderSn(String orderSn) {
        LambdaQueryWrapper<PayDO> queryWrapper = Wrappers.lambdaQuery(PayDO.class)
                .eq(PayDO::getOrderSn, orderSn);
        PayDO payDO = payMapper.selectOne(queryWrapper);
        return BeanUtil.convert(payDO, PayInfoRespDTO.class);
    }

    @Override
    public PayInfoRespDTO getPayInfoByPaySn(String paySn) {
        LambdaQueryWrapper<PayDO> queryWrapper = Wrappers.lambdaQuery(PayDO.class)
                .eq(PayDO::getPaySn, paySn);
        PayDO payDO = payMapper.selectOne(queryWrapper);
        return BeanUtil.convert(payDO, PayInfoRespDTO.class);
    }

    /**
     * @param payRequest
     * @return
     */
    @Override
    public PayRespDTO commonPay(PayRequest payRequest) {
        return null;
    }

    /**
     * @param payRequest
     * @return
     */
    @Override
    public PayRespDTO ToAliPay(PayRequest payRequest) {
        return null;
    }
}
