package org.wjx.service.Impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.wjx.core.CacheLoader;
import org.wjx.core.SafeCache;
import org.wjx.dao.DO.RegionDO;
import org.wjx.dao.DO.StationDO;
import org.wjx.dao.mapper.StationMapper;
import org.wjx.dto.req.RegionStationQueryReqDTO;
import org.wjx.dto.resp.RegionStationQueryRespDTO;
import org.wjx.dto.resp.StationQueryRespDTO;
import org.wjx.service.RegionStationService;
import org.wjx.utils.BeanUtil;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.wjx.constant.RedisKeyConstant.*;
import static org.wjx.constant.SystemConstant.ADVANCE_TICKET_DAY;

/**
 * @author xiu
 * @create 2023-11-29 9:49
 */
@Service
@RequiredArgsConstructor
public class RegionStationServiceImpl implements RegionStationService {
    final SafeCache safeCache;
    final StationMapper stationMapper;
    final RedissonClient redissonClient;

    @Override
    public List<RegionStationQueryRespDTO> listRegionStation(RegionStationQueryReqDTO requestParam) {
        String key;
        if (StringUtils.hasLength(requestParam.getName())) {
            key = REGION_STATION + requestParam.getName();
            return safeGetRegionStation(key, () -> {
                List<StationDO> stationDOS = stationMapper.GetlistRegionStationByFuzzyNameOrFuzzySpell(requestParam.getName());
                List<RegionStationQueryRespDTO> list = BeanUtil.convertToList(stationDOS, RegionStationQueryRespDTO.class);
                String jsonString = JSON.toJSONString(list);
                return jsonString;
            }, requestParam.getName());
        }
        key = REGION_STATION + requestParam.getQueryType();
        return safeGetRegionStation(key, () -> {
            List<RegionDO> regionDOS = stationMapper.GetlistRegionByType(requestParam.getQueryType());
            List<RegionStationQueryRespDTO> regionStationQueryRespDTOS = BeanUtil.convertToList(regionDOS, RegionStationQueryRespDTO.class);
            String jsonString = JSON.toJSONString(regionStationQueryRespDTOS);
            return jsonString;
        }, requestParam.getQueryType() + "");
    }

    /**
     * 查询所有的车站转为StationQueryRespDTO,并且保存到缓存
     */
    @Override
    public List<StationQueryRespDTO> listAllStation() {
        return safeCache.safeGet(STATION_ALL, List.class, ADVANCE_TICKET_DAY, TimeUnit.DAYS,
                () -> {
                    List<StationDO> stationDOS = stationMapper.selectList(new QueryWrapper<StationDO>());
                    return BeanUtil.convertToList(stationDOS, StationQueryRespDTO.class);
                });
    }

    private List<RegionStationQueryRespDTO> safeGetRegionStation(String key, CacheLoader<String> loader, String param) {
        String s = safeCache.get(key, String.class);
        List<RegionStationQueryRespDTO> res = JSON.parseArray(s, RegionStationQueryRespDTO.class);
        if (!CollUtil.isEmpty(res)) {
            return res;
        }
        String lockKey = String.format(LOCK_QUERY_REGION_STATION_LIST, param);
        RLock lock = redissonClient.getLock(lockKey);
        boolean b = lock.tryLock();
        try {
            if (b) {
                res = JSON.parseArray(s, RegionStationQueryRespDTO.class);
                if (CollUtil.isEmpty(res = loadAndSet(key, loader))) {
                    return Collections.emptyList();
                }
            }
        } finally {
            lock.unlock();
        }
        return res;
    }

    private List<RegionStationQueryRespDTO> loadAndSet(String key, CacheLoader<String> loader) {
        List<RegionStationQueryRespDTO> regionStationQueryRespDTOS = JSON.parseArray(loader.load(), RegionStationQueryRespDTO.class);
        if (CollUtil.isEmpty(regionStationQueryRespDTOS)) {
            return Collections.emptyList();
        }
        safeCache.put(
                key,
                loader.load(),
                ADVANCE_TICKET_DAY,
                TimeUnit.DAYS
        );
        return regionStationQueryRespDTOS;
    }
}
