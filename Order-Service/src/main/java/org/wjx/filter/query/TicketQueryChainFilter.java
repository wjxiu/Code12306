package org.wjx.filter.query;

import org.wjx.dto.req.TicketPageQueryReqDTO;
import org.wjx.filter.AbstractFilter;

/**
 * @author xiu
 * @create 2023-11-28 16:00
 */
public interface TicketQueryChainFilter<T extends TicketPageQueryReqDTO> extends AbstractFilter<TicketPageQueryReqDTO> {
    @Override
    default String mark(){
        return  "TicketQueryChainFilter";
    }
}
