package org.wjx.delay;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.wjx.Exception.ServiceException;
import org.wjx.dao.DO.PayDO;
import org.wjx.dao.mapper.PayMapper;
import org.wjx.enums.TradeStatusEnum;
import org.wjx.remote.OrderServiceRemote;

import static org.wjx.config.RabbitConfig.pay_Queue;

/**
 * 延迟支付队列取出来了的操作
 * @author xiu
 * @create 2023-12-22 20:20
 */
@Component
@RequiredArgsConstructor
public class payComplete {
    final PayMapper payMapper;
    final OrderServiceRemote orderServiceRemote;

    @RabbitListener(queues = pay_Queue)
    @Transactional
    public void listen(PayDO payDO,Channel channel, Message message){
//        todo 加锁保证一致性
        payDO.setStatus(TradeStatusEnum.TRADE_FINISHED.tradeCode());
        int update = payMapper.update(payDO, new LambdaQueryWrapper<PayDO>().eq(PayDO::getOrderSn, payDO.getOrderSn()));
        if (update<0)throw new ServiceException("延迟支付消费失败");
        Boolean b = orderServiceRemote.changeOrderAndOrderItemStatus(payDO.getOrderSn(),TradeStatusEnum.TRADE_SUCCESS.tradeCode() , TradeStatusEnum.TRADE_FINISHED.tradeCode());
        if (!b) throw new ServiceException("延迟支付消费失败");
    }
}
