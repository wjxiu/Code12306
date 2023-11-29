package org.wjx.filter.query;

import org.apache.commons.lang3.StringUtils;
import org.wjx.Exception.ClientException;
import org.wjx.dto.req.TicketPageQueryReqDTO;

/**
 * @author xiu
 * @create 2023-11-28 16:27
 */
public class TrainTicketQueryParamNotNullChainFilter implements TicketQueryChainFilter<TicketPageQueryReqDTO>{
    /**
     * 定义过滤逻辑
     *
     * @param requestParam 被过滤的数据
     */
    @Override
    public void handle(TicketPageQueryReqDTO requestParam) {
        if (StringUtils.isBlank(requestParam.getFromStation())) {
            throw new ClientException("出发地不能为空");
        }
        if (StringUtils.isBlank(requestParam.getToStation())) {
            throw new ClientException("目的地不能为空");
        }
        if (requestParam.getDepartureDate() == null) {
            throw new ClientException("出发日期不能为空");
        }
    }

    /**
     * @return
     */
    @Override
    public int getOrder() {
        return 1;
    }
}
