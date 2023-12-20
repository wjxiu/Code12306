package org.wjx.filter.query;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wjx.Exception.ClientException;
import org.wjx.dao.DO.RegionDO;
import org.wjx.dao.DO.StationDO;
import org.wjx.dao.DO.TrainDO;
import org.wjx.dao.mapper.RegionMapper;
import org.wjx.dao.mapper.StationMapper;
import org.wjx.dao.mapper.TrainMapper;
import org.wjx.dto.req.TicketPageQueryReqDTO;

/**
 * @author xiu
 * @create 2023-11-28 16:27
 */
@Component@RequiredArgsConstructor
public class TrainTicketQueryParamNotNullChainFilter implements TicketQueryChainFilter<TicketPageQueryReqDTO>{
    final RedissonClient redissonClient;
    final RegionMapper regionMapper;
    final StationMapper stationMapper;
    final TrainMapper trainMapper;
    /**
     * 定义过滤逻辑
     *
     * @param requestParam 被过滤的数据
     */
    @Override
    public void handle(TicketPageQueryReqDTO requestParam) {
        String fromStation = requestParam.getFromStation();
        String toStation = requestParam.getToStation();
        if (StringUtils.isBlank(fromStation)) throw new ClientException("出发地不能为空");
        if (StringUtils.isBlank(toStation)) throw new ClientException("目的地不能为空");
        if (requestParam.getDepartureDate() == null) throw new ClientException("出发日期不能为空");
        if (StringUtils.isBlank(requestParam.getArrival())) throw new ClientException("到达站点不能为空");
//        RBloomFilter<Object> BloomFilter = redissonClient.getBloomFilter("RegionCodeNotNull");
//        RBloomFilter<Object> StationCodeNotNullBloomFilter = redissonClient.getBloomFilter("StationCodeNotNull");
//        if (!BloomFilter.contains(fromStation)||BloomFilter.contains(toStation)) throw new ClientException("出发地或目的地不存在");
//        if (!StationCodeNotNullBloomFilter.contains(requestParam.getArrival())||!StationCodeNotNullBloomFilter.contains(requestParam.getArrival())) throw new ClientException("出发站点或到达站点不存在");
    }

    /**
     * 晚上一点初始化全部站点数据
     */
    @Scheduled(cron = "0 0 1 1 * *")
    public void initRegionCodeNotNullbloomfilter(){
        RBloomFilter<Object> BloomFilter = redissonClient.getBloomFilter("TrainNameNotNull");
        for (RegionDO regionDO : regionMapper.selectList(new LambdaQueryWrapper<RegionDO>().select(RegionDO::getCode))) {
            BloomFilter.add(regionDO.getCode());
        }
    }
    @Scheduled(cron = "0 0 2 1 * *")
    public void initStationCodeNotNullbloomfilter(){
        RBloomFilter<Object> BloomFilter = redissonClient.getBloomFilter("StationCodeNotNull");
        for (StationDO stationDO : stationMapper.selectList(new LambdaQueryWrapper<StationDO>().select(StationDO::getCode))) {
            BloomFilter.add(stationDO.getCode());
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
