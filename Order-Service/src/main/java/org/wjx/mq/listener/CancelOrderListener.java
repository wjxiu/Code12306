package org.wjx.mq.listener;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.wjx.dto.event.DelayCloseOrderEvent;
import org.wjx.service.OrderService;

import java.io.IOException;

import static org.wjx.config.RabbitConfig.creatOrder_delayed_queue;

/**
 * @author xiu
 * @create 2023-12-08 14:15
 */
@Component@RequiredArgsConstructor
@Slf4j
public class CancelOrderListener {
    final OrderService orderService;
//    15分钟后执行取消订单操作
    @RabbitListener(queues = creatOrder_delayed_queue)
    public void listenCreate(DelayCloseOrderEvent delayCloseOrderEvent, Channel channel, Message message) throws IOException {
        try {
            orderService.cancelOrCloseTickOrder(delayCloseOrderEvent.getOrderSn());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        }catch (Exception e){
            log.info("消息处理出错-------{}",message);
            log.info("异常信息-------{}",e.getMessage());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
