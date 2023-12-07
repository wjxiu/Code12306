package org.wjx.service.Impl;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wjx.Res;
import org.wjx.dao.DO.OrderDO;
import org.wjx.dao.DO.OrderItemDO;
import org.wjx.dao.DO.OrderItemPassengerDO;
import org.wjx.dao.mapper.OrderItemMapper;
import org.wjx.dao.mapper.OrderMapper;
import org.wjx.dao.mapper.OrderItemPassengerMapper;
import org.wjx.dto.req.TicketOrderPageQueryReqDTO;
import org.wjx.dto.resp.TicketOrderDetailRespDTO;
import org.wjx.dto.resp.TicketOrderDetailSelfRespDTO;
import org.wjx.dto.resp.TicketOrderPassengerDetailRespDTO;
import org.wjx.enums.OrderStatusEnum;
import org.wjx.page.PageRequest;
import org.wjx.page.PageResponse;
import org.wjx.remote.UserRemoteService;
import org.wjx.remote.dto.UserQueryActualRespDTO;
import org.wjx.service.OrderItemService;
import org.wjx.service.OrderService;
import org.wjx.user.core.UserContext;
import org.wjx.utils.BeanUtil;
import org.wjx.utils.PageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiu
 * @create 2023-12-07 12:18
 */
@Service@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    final OrderItemMapper orderItemMapper;
    final OrderMapper orderMapper;
    final OrderItemService orderItemService;
    final UserRemoteService userRemoteService;
    final OrderItemPassengerMapper orderItemPassengerMapper;
    /**
     * 跟据订单号查询车票订单
     *
     * @param orderSn 订单号
     * @return 订单详情
     */
    @Override
    public TicketOrderDetailRespDTO queryTicketOrderByOrderSn(String orderSn) {
        OrderDO orderDO = orderMapper.selectOne(new LambdaQueryWrapper<OrderDO>().eq(OrderDO::getOrderSn, orderSn));
        if (orderDO==null) return null;
        LambdaQueryWrapper<OrderItemDO> lambdaQueryWrapper=new LambdaQueryWrapper<OrderItemDO>()
                .eq(OrderItemDO::getOrderSn,orderSn);
        List<OrderItemDO> orderItemDOS = orderItemMapper.selectList(lambdaQueryWrapper);
        TicketOrderDetailRespDTO result = BeanUtil.convert(orderDO, TicketOrderDetailRespDTO.class);
        result.setPassengerDetails(BeanUtil.convertToList(orderItemDOS, TicketOrderPassengerDetailRespDTO.class));
        return result;
    }

    /**
     * 分页查询订单
     * @param requestParam
     * @return
     */
    @Override
    public PageResponse<TicketOrderDetailRespDTO> pageTicketOrder(TicketOrderPageQueryReqDTO requestParam) {
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
      return  PageUtil.convert(page,TicketOrderDetailSelfRespDTO.class, orderItemPassengerDO ->{
            String orderSn = orderItemPassengerDO.getOrderSn();
            OrderDO orderDO = orderMapper.selectOne(new LambdaQueryWrapper<OrderDO>().eq(OrderDO::getOrderSn, orderSn));
            OrderItemDO orderItemDO = orderItemMapper.selectOne(new LambdaQueryWrapper<OrderItemDO>().eq(OrderItemDO::getOrderSn, orderDO).eq(OrderItemDO::getIdCard, idCard).eq(OrderItemDO::getIdType, idType));
            TicketOrderDetailSelfRespDTO res = BeanUtil.convert(orderDO, TicketOrderDetailSelfRespDTO.class);
            BeanUtil.convertIgnoreNullAndBlank(orderItemDO,res);
            return res;
        });
    }
    private List<Integer> buildOrderStatusList(TicketOrderPageQueryReqDTO requestParam) {
        List<Integer> result = new ArrayList<>();
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
