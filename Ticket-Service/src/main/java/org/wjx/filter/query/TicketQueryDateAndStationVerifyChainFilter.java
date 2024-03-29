package org.wjx.filter.query;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wjx.Exception.ClientException;
import org.wjx.dao.DO.TrainDO;
import org.wjx.dao.mapper.RegionMapper;
import org.wjx.dao.mapper.StationMapper;
import org.wjx.dao.mapper.TrainMapper;
import org.wjx.dto.req.TicketPageQueryReqDTO;

import java.util.Date;

/**
 * 过滤若其
 * @author xiu
 * @create 2023-11-28 16:13
 */
@Component@RequiredArgsConstructor
public class TicketQueryDateAndStationVerifyChainFilter implements TicketQueryChainFilter<TicketPageQueryReqDTO>{

    /**
     * 定义过滤逻辑
     *
     * @param reqParam 被过滤的数据
     */
    @Override
    public void handle(TicketPageQueryReqDTO reqParam) {
       if (reqParam.getFromStation().equals(reqParam.getToStation())){
            throw new ClientException("禁止出发站点和目的站点相同");
        }
    }


    @Override
    public int getOrder() {
        return 5;
    }
}
