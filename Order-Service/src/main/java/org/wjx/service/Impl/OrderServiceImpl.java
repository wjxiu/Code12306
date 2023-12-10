package org.wjx.service.Impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.IdUtil;
import cn.hutool.db.sql.Order;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wjx.Exception.ServiceException;
import org.wjx.Res;
//import org.wjx.mq.listener.MessageSender;
import org.wjx.annotation.Idempotent;
import org.wjx.dao.DO.OrderDO;
import org.wjx.dao.DO.OrderItemDO;
import org.wjx.dao.DO.OrderItemPassengerDO;
import org.wjx.dao.mapper.OrderItemMapper;
import org.wjx.dao.mapper.OrderMapper;
import org.wjx.dao.mapper.OrderItemPassengerMapper;
import org.wjx.dto.event.DelayCloseOrderEvent;
import org.wjx.dto.req.TicketOrderCreateReqDTO;
import org.wjx.dto.req.TicketOrderItemCreateReqDTO;
import org.wjx.dto.req.TicketOrderPageQueryReqDTO;
import org.wjx.dto.resp.TicketOrderDetailRespDTO;
import org.wjx.dto.resp.TicketOrderDetailSelfRespDTO;
import org.wjx.dto.resp.TicketOrderPassengerDetailRespDTO;
import org.wjx.enums.IdempotentTypeEnum;
import org.wjx.enums.OrderStatusEnum;
import org.wjx.page.PageRequest;
import org.wjx.page.PageResponse;
import org.wjx.remote.UserRemoteService;
import org.wjx.remote.dto.UserQueryActualRespDTO;
import org.wjx.service.OrderItemPassengerService;
import org.wjx.service.OrderItemService;
import org.wjx.service.OrderService;
import org.wjx.user.core.UserContext;
import org.wjx.utils.BeanUtil;
import org.wjx.utils.PageUtil;

import java.util.ArrayList;
import java.util.List;

import static org.wjx.config.RabbitConfig.createOrder_routingkey;
import static org.wjx.config.RabbitConfig.exchange_delayed;

/**
 * @author xiu
 * @create 2023-12-07 12:18
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    final OrderItemMapper orderItemMapper;
    final OrderMapper orderMapper;
    final OrderItemService orderItemService;
    final UserRemoteService userRemoteService;
    final OrderItemPassengerMapper orderItemPassengerMapper;
    final OrderItemPassengerService orderItemPassengerService;
    final RabbitTemplate rabbitTemplate;
    final RedissonClient redissonClient;
//    final MessageSender messageSender;

    /**
     * 跟据订单号查询车票订单
     *
     * @param orderSn 订单号
     * @return 订单详情
     */
    @Override
    public TicketOrderDetailRespDTO queryTicketOrderByOrderSn(String orderSn) {
        OrderDO orderDO = orderMapper.selectOne(new LambdaQueryWrapper<OrderDO>().eq(OrderDO::getOrderSn, orderSn));
        if (orderDO == null) return null;
        LambdaQueryWrapper<OrderItemDO> lambdaQueryWrapper = new LambdaQueryWrapper<OrderItemDO>()
                .eq(OrderItemDO::getOrderSn, orderSn);
        List<OrderItemDO> orderItemDOS = orderItemMapper.selectList(lambdaQueryWrapper);
        TicketOrderDetailRespDTO result = BeanUtil.convert(orderDO, TicketOrderDetailRespDTO.class);
        result.setPassengerDetails(BeanUtil.convertToList(orderItemDOS, TicketOrderPassengerDetailRespDTO.class));
        return result;
    }

    /**
     * 分页查询订单
     *
     * @param requestParam
     * @return
     */
    @Override
    public PageResponse<TicketOrderDetailRespDTO> pageTicketOrder(TicketOrderPageQueryReqDTO requestParam) {
        log.info("TicketOrderPageQueryReqDTO:{}",requestParam.toString());
        LambdaQueryWrapper<OrderDO> queryWrapper = Wrappers.lambdaQuery(OrderDO.class)
                .eq(OrderDO::getUserId, requestParam.getUserId())
                .in(OrderDO::getStatus, buildOrderStatusList(requestParam))
                .orderByDesc(OrderDO::getOrderTime);
        IPage<OrderDO> page = orderMapper.selectPage(PageUtil.convert(requestParam), queryWrapper);
        PageResponse<TicketOrderDetailRespDTO> convert = PageUtil.convert(page, TicketOrderDetailRespDTO.class, each -> {
            TicketOrderDetailRespDTO convert1 = BeanUtil.convert(each, TicketOrderDetailRespDTO.class);
            List<OrderItemDO> orderItemDOS = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItemDO>().eq(OrderItemDO::getOrderSn, each.getOrderSn()));
            convert1.setPassengerDetails(BeanUtil.convertToList(orderItemDOS, TicketOrderPassengerDetailRespDTO.class));
            return convert1;
        });
        return convert;
    }

    /**
     * @param requestParam
     * @return
     */
    @Override
    public PageResponse<TicketOrderDetailSelfRespDTO> pageSelfTicketOrder(PageRequest requestParam) {
//        先查出用户对应的所有乘车人的证件和证件类型
        Res<UserQueryActualRespDTO> userQueryActualRespDTORes = userRemoteService.queryActualUserByUsername(UserContext.getUserName());
        UserQueryActualRespDTO data = userQueryActualRespDTORes.getData();
        String idCard = data.getIdCard();
        Integer idType = data.getIdType();
        IPage<OrderItemPassengerDO> page = orderItemPassengerMapper.selectPage(PageUtil.convert(requestParam), new LambdaQueryWrapper<OrderItemPassengerDO>()
                .eq(OrderItemPassengerDO::getIdCard, idCard).eq(OrderItemPassengerDO::getIdType, idType));
        return PageUtil.convert(page, TicketOrderDetailSelfRespDTO.class, orderItemPassengerDO -> {
            String orderSn = orderItemPassengerDO.getOrderSn();
            OrderDO orderDO = orderMapper.selectOne(new LambdaQueryWrapper<OrderDO>().eq(OrderDO::getOrderSn, orderSn));
            OrderItemDO orderItemDO = orderItemMapper.selectOne(new LambdaQueryWrapper<OrderItemDO>().eq(OrderItemDO::getOrderSn, orderDO).eq(OrderItemDO::getIdCard, idCard).eq(OrderItemDO::getIdType, idType));
            TicketOrderDetailSelfRespDTO res = BeanUtil.convert(orderDO, TicketOrderDetailSelfRespDTO.class);
            BeanUtil.convertIgnoreNullAndBlank(orderItemDO, res);
            return res;
        });
    }

    /**
     * 15分钟
     */
    private static final int delaytime = 1 * 2 * 1000;

    /**
     * 创建火车票订单
     *
     * @param requestParam
     * @return 订单号
     */
    @Override
    public String createTicketOrder(TicketOrderCreateReqDTO requestParam) {
        String orderSn = String.valueOf(IdUtil.getSnowflakeNextId());
        OrderDO orderDO = convertToOrderDO(requestParam, orderSn);
        orderMapper.insert(orderDO);
        List<TicketOrderItemCreateReqDTO> ticketOrderItems = requestParam.getTicketOrderItems();
        List<OrderItemDO> orderItemDOList = new ArrayList<>();
        List<OrderItemPassengerDO> orderPassengerRelationDOList = new ArrayList<>();
        ticketOrderItems.forEach(each -> {
            OrderItemDO orderItemDO = convertToOrderItemDO(requestParam, each, orderSn);
            orderItemDOList.add(orderItemDO);
            OrderItemPassengerDO orderPassengerRelationDO = OrderItemPassengerDO.builder()
                    .idType(each.getIdType())
                    .idCard(each.getIdCard())
                    .orderSn(orderSn)
                    .build();
            orderPassengerRelationDOList.add(orderPassengerRelationDO);
        });
        orderItemService.saveBatch(orderItemDOList);
        orderItemPassengerService.saveBatch(orderPassengerRelationDOList);
        try {
//            发送延时消息，15分钟后不支付就关闭订单
            DelayCloseOrderEvent build = DelayCloseOrderEvent.builder().arrival(requestParam.getArrival()).departure(requestParam.getDeparture())
                    .trainId(requestParam.getTrainId() + "").orderSn(orderSn).trainPurchaseTicketResults(requestParam.getTicketOrderItems()).build();
            rabbitTemplate.convertAndSend(exchange_delayed, createOrder_routingkey, build,message -> {
                //设置消息持久化
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                message.getMessageProperties().setDelay(delaytime);
                return message;
            });
        } catch (Throwable ex) {
            log.error("延迟关闭订单消息队列发送错误，请求参数：{}", JSON.toJSONString(requestParam), ex);
            throw ex;
        }
        return orderSn;
    }

    /**
     * 取消订单
     * 更新订单号对应的订单和订单项的状态为取消(30)
     *
     * @param orderSn 订单号
     * @return 全部更新成功返回true, 否则返回false
     */
    @Override
    @SneakyThrows
    public boolean cancelOrCloseTickOrder(String orderSn) {
        RLock lock = redissonClient.getLock("cancelOrder::" + orderSn);
        boolean b = lock.tryLock();
        try {
            if (b){
                OrderDO order = new OrderDO();
                order.setStatus(OrderStatusEnum.CLOSED.getStatus());
                int update = orderMapper.update(order, new LambdaUpdateWrapper<OrderDO>()
                        .eq(OrderDO::getOrderSn, orderSn)
                        .eq(OrderDO::getStatus, OrderStatusEnum.PENDING_PAYMENT.getStatus()));
                if (update <=0) throw new ServiceException("订单已经改变状态");
                OrderItemDO orderItemDO = new OrderItemDO();
                orderItemDO.setOrderSn(orderSn);
                orderItemDO.setStatus(OrderStatusEnum.CLOSED.getStatus());
                orderItemDO.setStatus(order.getStatus());
                int updatecount = orderItemMapper.update(orderItemDO, new LambdaQueryWrapper<OrderItemDO>()
                        .eq(OrderItemDO::getOrderSn, orderItemDO.getOrderSn())
                        .eq(OrderItemDO::getStatus, OrderStatusEnum.PENDING_PAYMENT.getStatus()));
                if (updatecount <= 0) throw new ServiceException("订单已经改变状态");
                return true;
            }else{
                throw new ServiceException("重复点击");
            }
        }finally {
            lock.unlock();
        }
    }
    private OrderItemDO convertToOrderItemDO(TicketOrderCreateReqDTO requestParam, TicketOrderItemCreateReqDTO each, String orderSn) {
        OrderItemDO orderItemDO = OrderItemDO.builder()
                .trainId(requestParam.getTrainId())
                .seatNumber(each.getSeatNumber())
                .carriageNumber(each.getCarriageNumber())
                .realName(each.getRealName())
                .orderSn(orderSn)
                .phone(each.getPhone())
                .seatType(each.getSeatType())
                .username(requestParam.getUsername()).amount(each.getAmount()).carriageNumber(each.getCarriageNumber())
                .idCard(each.getIdCard())
                .ticketType(each.getTicketType())
                .idType(each.getIdType())
                .userId(String.valueOf(requestParam.getUserId()))
                .status(0)
                .build();
        return orderItemDO;
    }

    private OrderDO convertToOrderDO(TicketOrderCreateReqDTO requestParam, String orderSn) {
        OrderDO orderDO = OrderDO.builder().orderSn(orderSn)
                .orderTime(requestParam.getOrderTime())
                .departure(requestParam.getDeparture())
                .departureTime(requestParam.getDepartureTime())
                .ridingDate(requestParam.getRidingDate())
                .arrivalTime(requestParam.getArrivalTime())
                .trainNumber(requestParam.getTrainNumber())
                .arrival(requestParam.getArrival())
                .trainId(requestParam.getTrainId())
                .source(requestParam.getSource())
                .status(OrderStatusEnum.PENDING_PAYMENT.getStatus())
                .username(requestParam.getUsername())
                .userId(String.valueOf(requestParam.getUserId()))
                .build();
        return orderDO;
    }

    private List<Integer> buildOrderStatusList(TicketOrderPageQueryReqDTO requestParam) {
        List<Integer> result = new ArrayList<>();
        log.info("requestParam::{}",requestParam.toString());
        switch (requestParam.getStatusType()) {
            case 0 -> result = ListUtil.of(
                    OrderStatusEnum.PENDING_PAYMENT.getStatus()
            );
            case 1 -> result = ListUtil.of(
                    OrderStatusEnum.ALREADY_PAID.getStatus(),
                    OrderStatusEnum.PARTIAL_REFUND.getStatus(),
                    OrderStatusEnum.FULL_REFUND.getStatus()
            );
            case 2 -> result = ListUtil.of(
                    OrderStatusEnum.COMPLETED.getStatus()
            );
        }
        return result;
    }
}
