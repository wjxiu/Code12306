package org.wjx.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.wjx.common.TicketChainMarkEnum;
import org.wjx.core.SafeCache;
import org.wjx.dto.entiey.TicketListDTO;
import org.wjx.dto.req.CancelTicketOrderReqDTO;
import org.wjx.dto.req.PurchaseTicketReqDTO;
import org.wjx.dto.req.RefundTicketReqDTO;
import org.wjx.dto.req.TicketPageQueryReqDTO;
import org.wjx.dto.resp.*;
import org.wjx.filter.AbstractFilterChainsContext;
import org.wjx.service.TicketService;

import java.util.List;

/**
 * @author xiu
 * @create 2023-11-28 15:16
 */
@Service@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    final AbstractFilterChainsContext chainsContext;
    final SafeCache cache;
    /**
     * 根据条件分页查询车票
     * @param requestParam 分页查询车票请求参数
     * @return 查询车票返回结果
     */
    @Override
    public TicketPageQueryRespDTO pageListTicketQueryV1(TicketPageQueryReqDTO requestParam) {
        return null;
    }

    /**
     * 根据条件分页查询车票(高性能)
     * @param requestParam 分页查询车票请求参数
     * @return 查询车票返回结果
     */
    @Override
    public TicketPageQueryRespDTO pageListTicketQueryV2(TicketPageQueryReqDTO requestParam) {
//        通过职责链模式过滤参数
        chainsContext.execute(TicketChainMarkEnum.TRAIN_QUERY_FILTER.name(),requestParam);
        StringRedisTemplate instance =(StringRedisTemplate) cache.getInstance();
        List<TicketListDTO> seatResults =getTicketListDTOS(instance,requestParam);
        return null;
    }

    private List<TicketListDTO> getTicketListDTOS(StringRedisTemplate instance, TicketPageQueryReqDTO requestParam) {
//todo
        return null;
    }

    /**
     * 购买车票v1
     * @param requestParam 车票购买请求参数
     * @return 订单好
     */
    @Override
    public TicketPurchaseRespDTO purchaseTicketsV1(PurchaseTicketReqDTO requestParam) {
        return null;
    }

    /**
     * 购买车票v2(高性能)
     *
     * @param requestParam 车票购买请求参数
     * @return 订单号
     */
    @Override
    public TicketPurchaseRespDTO purchaseTicketsV2(PurchaseTicketReqDTO requestParam) {
        RedisTemplate instance = cache.getInstance();
        return null;
    }

    /**
     * 取消车票
     *
     * @param requestParam 车票取消请求参数
     */
    @Override
    public void cancelTicketOrder(CancelTicketOrderReqDTO requestParam) {

    }

    /**
     * 查询支付单详情查询
     *
     * @param orderSn 订单号
     * @return 支付单详情查询
     */
    @Override
    public PayInfoRespDTO getPayInfo(String orderSn) {
        return null;
    }

    /**
     * 公共退款接口
     *
     * @param requestParam 退款请求参数
     * @return 退款返回详情
     */
    @Override
    public RefundTicketRespDTO commonTicketRefund(RefundTicketReqDTO requestParam) {
        return null;
    }
}
