package org.wjx.mq.listener;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.aop.framework.AopProxy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.wjx.Exception.ClientException;
import org.wjx.Exception.ServiceException;
import org.wjx.dao.DO.OrderDO;
import org.wjx.dao.DO.OrderItemDO;
import org.wjx.dao.mapper.OrderItemMapper;
import org.wjx.dao.mapper.OrderMapper;
import org.wjx.dto.event.DelayCloseOrderEvent;
import org.wjx.remote.SeatRemoteService;
import org.wjx.remote.dto.ResetSeatDTO;
import org.wjx.service.OrderService;
import org.wjx.user.core.ApplicationContextHolder;
import org.wjx.utils.BeanUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.wjx.config.RabbitConfig.*;

/**
 * @author xiu
 * @create 2023-12-08 14:15
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CancelOrderListener {
    final OrderService orderService;
    final SeatRemoteService seatRemoteService;
    final OrderItemMapper orderItemServicemapper;
    final OrderMapper orderMapper;
    int maxretrycount=3;
    //    15分钟后执行取消订单操作,自动取消之后需要恢复座位状态
    @RabbitListener(queues = creatOrder_delayed_queue)
    @Transactional(rollbackFor = Throwable.class)
    public void listenCreate(DelayCloseOrderEvent delayCloseOrderEvent, Channel channel, Message message) throws IOException {
        log.info("开始取消订单");
        MessageProperties messageProperties = message.getMessageProperties();
        Optional.ofNullable(messageProperties.getHeader("retry-count"))
                .map(retryCount -> (int) retryCount)
                .ifPresent(retryCount -> {
                    if (retryCount <= maxretrycount) {
                        throw new ClientException("订单重试过多");
                    }
                });
        try {
            orderService.cancelOrCloseTickOrder(delayCloseOrderEvent.getOrderSn());
            List<OrderItemDO> orderItemDOS = orderItemServicemapper.selectList(new LambdaQueryWrapper<OrderItemDO>()
                    .eq(OrderItemDO::getOrderSn, delayCloseOrderEvent.getOrderSn()));
            List<ResetSeatDTO> resetSeatDTOS = BeanUtil.convertToList(orderItemDOS, ResetSeatDTO.class);
            log.info("订单号:{}",delayCloseOrderEvent.getOrderSn());
            OrderDO orderDO = orderMapper.selectOne(new LambdaQueryWrapper<OrderDO>().eq(OrderDO::getOrderSn, delayCloseOrderEvent.getOrderSn()));
            resetSeatDTOS.forEach(a->{a.setStartStation(orderDO.getDeparture());a.setEndStation(orderDO.getArrival());});
            log.info("传递取消订单dto:{}",resetSeatDTOS);
            /*
               optimize
               这里报错:没有token,(暂时通过加入白名单跳过错误)
             */
            seatRemoteService.ResetSeatStatus(resetSeatDTOS);
            channel.basicAck(messageProperties.getDeliveryTag(), false);
        } catch (Exception e) {
            if (messageProperties.getHeader("retry-count")!=null){
//             发送重试
                    messageProperties.setHeader("retry-count", (int)messageProperties.getHeader("retry-count") +1);
                    CancelOrderListener CancelOrderListener = ApplicationContextHolder.getBean(CancelOrderListener.class);
                    CancelOrderListener.listenCreate(delayCloseOrderEvent,channel,message);
//                    todo 未验证是否立即发送给接收者
                    channel.basicPublish(exchange_delayed,createOrder_routingkey, null,JSON.toJSONBytes(delayCloseOrderEvent));
            }else{
                messageProperties.setHeader("retry-count",1);
            }
            channel.basicAck(messageProperties.getDeliveryTag(), false);
            throw new ServiceException("订单取消失败，正在重试");
        }
        log.info("取消订单结束");
    }
}
