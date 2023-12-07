package org.wjx.filter.purchase;

import org.wjx.dto.req.PurchaseTicketReqDTO;
import org.wjx.filter.AbstractFilter;
import org.wjx.filter.AbstractFilterChainsContext;

/**
 * @author xiu
 * @create 2023-12-04 19:40
 */
public interface TrainPurchaseTicketChainFilter<T extends PurchaseTicketReqDTO> extends AbstractFilter<PurchaseTicketReqDTO> {

    /**
     * 设置过滤器的名字,相同名字的被认为是同一组过滤器
     * 推荐使用一个接口定义名字,保存在AbstractFilterChainsContext中
     */
    @Override
     default String mark() {

        return "TrainPurchaseTicketChainFilter";
    }
}
