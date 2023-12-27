package org.wjx.Service.Impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wjx.Exception.ServiceException;
import org.wjx.PayInfoRespDTO;
import org.wjx.Service.PayService;
import org.wjx.dao.DO.PayDO;
import org.wjx.dao.mapper.PayMapper;
import org.wjx.dto.PayRequest;
import org.wjx.dto.PayRespDTO;
import org.wjx.enums.OrderStatusEnum;
import org.wjx.enums.TradeStatusEnum;
import org.wjx.remote.OrderServiceRemote;
import org.wjx.utils.BeanUtil;

import java.time.LocalDateTime;

import static org.wjx.config.RabbitConfig.*;

/**
 * @author xiu
 * @create 2023-12-10 19:21
 */
@RequiredArgsConstructor
@Service
public class PayServiceImpl implements PayService {
    final PayMapper payMapper;
    final OrderServiceRemote orderServiceRemote;
    final RabbitTemplate rabbitTemplate;
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
    @Transactional(rollbackFor = Throwable.class)
    public PayRespDTO commonPay(PayRequest payRequest) {
        String orderSn = payRequest.getOrderSn();
        PayDO payDO = new PayDO();
        payDO.setOrderSn(orderSn);
        payDO.setId(IdUtil.getSnowflakeNextId());
        payDO.setStatus(OrderStatusEnum.ALREADY_PAID.getStatus());
        int uppdate = payMapper.insert(payDO);
        Boolean b = orderServiceRemote.changeOrderAndOrderItemStatus(orderSn, TradeStatusEnum.WAIT_BUYER_PAY.tradeCode(), TradeStatusEnum.TRADE_SUCCESS.tradeCode());
        if (uppdate<0||!b)throw new ServiceException("订单支付失败");
//        放到支付的延时队列里边
        rabbitTemplate.convertAndSend(exchange_delayed,pay_routingkey,payDO, message -> {
            //设置消息持久化
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
         LocalDateTime time=   orderServiceRemote.getDepartTimeByOrderSn(orderSn);
            LocalDateTime localDateTime = time.minusHours(2L);
            LocalDateTime currentDateTime = LocalDateTime.now();
            int millisecondsDifference =(int) DateUtil.betweenMs(DateUtil.date(currentDateTime), DateUtil.date(localDateTime));
//             获取火车出发时间，设置延迟时间为出发的前两个小时到现在的时间差，这里修改两小时后
            message.getMessageProperties().setDelay(millisecondsDifference);
            return message;
        });

        PayRespDTO payRespDTO = new PayRespDTO();
        payRespDTO.body="支付成功";
        return payRespDTO;
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
