package org.wjx.mq.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.wjx.dao.DO.OrderDO;
import org.wjx.dao.DO.OrderItemDO;
import org.wjx.dao.mapper.OrderItemMapper;
import org.wjx.dao.mapper.OrderMapper;
import org.wjx.dto.event.DelayCloseOrderEvent;
import org.wjx.remote.SeatRemoteService;
import org.wjx.remote.dto.ResetSeatDTO;
import org.wjx.service.OrderService;
import org.wjx.utils.BeanUtil;

import java.io.IOException;
import java.util.List;

import static org.wjx.config.RabbitConfig.creatOrder_delayed_queue;

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

    //    15分钟后执行取消订单操作,自动取消之后需要恢复座位状态
    @RabbitListener(queues = creatOrder_delayed_queue)
    public void listenCreate(DelayCloseOrderEvent delayCloseOrderEvent, Channel channel, Message message) throws IOException {
        log.info("开始取消订单");
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
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        } catch (Exception e) {
            log.info("异常信息-------{}", e.getMessage());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        }
        log.info("取消订单结束");
    }
}
