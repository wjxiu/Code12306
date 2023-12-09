package org.wjx.service.Impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.StrBuilder;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.wjx.Exception.ServiceException;
import org.wjx.dao.DO.OrderDO;
import org.wjx.dao.DO.OrderItemDO;
import org.wjx.dao.mapper.OrderItemMapper;
import org.wjx.dao.mapper.OrderMapper;
import org.wjx.dto.normal.OrderItemStatusReversalDTO;
import org.wjx.dto.req.TicketOrderItemQueryReqDTO;
import org.wjx.dto.resp.TicketOrderPassengerDetailRespDTO;
import org.wjx.service.OrderItemService;
import org.wjx.utils.BeanUtil;

import java.util.List;

/**
 * @author xiu
 * @create 2023-12-07 12:47
 */
@Service@RequiredArgsConstructor
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItemDO> implements OrderItemService  {
    final OrderItemMapper orderItemMapper;
    final OrderMapper orderMapper;
    final RedissonClient redissonClient;
    /**
     * 根据子订单记录id查询车票子订单详情
     *
     * @param requestParam
     */
    @Override
    public List<TicketOrderPassengerDetailRespDTO> queryTicketItemOrderById(TicketOrderItemQueryReqDTO requestParam) {
        LambdaQueryWrapper<OrderItemDO> in = new LambdaQueryWrapper<OrderItemDO>().eq(OrderItemDO::getOrderSn, requestParam.getOrderSn())
                .in(OrderItemDO::getId,requestParam.getOrderItemRecordIds());
        List<OrderItemDO> orderItemDOS = orderItemMapper.selectList(in);
        return  BeanUtil.convertToList(orderItemDOS, TicketOrderPassengerDetailRespDTO.class);
    }

    /**
     * 子订单状态反转
     *
     * @param requestParam 请求参数
     */
    @Override
    public void orderItemStatusReversal(OrderItemStatusReversalDTO requestParam) {
        LambdaQueryWrapper<OrderDO> queryWrapper = Wrappers.lambdaQuery(OrderDO.class)
                .eq(OrderDO::getOrderSn, requestParam.getOrderSn());
        OrderDO orderDO = orderMapper.selectOne(queryWrapper);
        if (orderDO == null) {
            throw new ServiceException("不存在该订单");
        }
        RLock lock = redissonClient.getLock(StrBuilder.create("order:status-reversal:order_sn_").append(requestParam.getOrderSn()).toString());
        if (!lock.tryLock()) {
            throw new ServiceException("订单重复修改状态，状态反转请求参数:"+JSON.toJSONString(requestParam));
        }
        try {
            OrderDO updateOrderDO = new OrderDO();
            updateOrderDO.setStatus(requestParam.getOrderStatus());
            LambdaUpdateWrapper<OrderDO> updateWrapper = Wrappers.lambdaUpdate(OrderDO.class)
                    .eq(OrderDO::getOrderSn, requestParam.getOrderSn());
            int orderUpdateResult = orderMapper.update(updateOrderDO, updateWrapper);
            if (orderUpdateResult <= 0) {
                throw new ServiceException("订单更新状态失败");
            }
            List<OrderItemDO> orderItemDOList = requestParam.getOrderItemDOList();
            if (CollectionUtil.isEmpty(orderItemDOList))return;
            orderItemDOList.forEach(o -> {
                OrderItemDO orderItemDO = new OrderItemDO();
                orderItemDO.setStatus(requestParam.getOrderItemStatus());
                LambdaUpdateWrapper<OrderItemDO> orderItemUpdateWrapper = Wrappers.lambdaUpdate(OrderItemDO.class)
                        .eq(OrderItemDO::getOrderSn, requestParam.getOrderSn())
                        .eq(OrderItemDO::getRealName, o.getRealName());
                int orderItemUpdateResult = orderItemMapper.update(orderItemDO, orderItemUpdateWrapper);
                if (orderItemUpdateResult <= 0) {
                    throw new ServiceException("订单项更新状态失败");
                }
            });
        }finally {
            lock.unlock();
        }
    }
}
