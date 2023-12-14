package org.wjx.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wjx.core.SafeCache;
import org.wjx.dao.DO.TrainDO;
import org.wjx.dao.mapper.TrainMapper;
import org.wjx.service.TrainService;

import java.util.concurrent.TimeUnit;

import static org.wjx.constant.RedisKeyConstant.TRAIN_INFO_BY_TRAINID;
import static org.wjx.constant.SystemConstant.ADVANCE_TICKET_DAY;

/**
 * @author xiu
 * @create 2023-12-14 20:49
 */
@Service
@RequiredArgsConstructor
public class TrainServiceImpl implements TrainService {
    final SafeCache cache;
    final TrainMapper trainMapper;

    /**
     * 根据列车id获取列车并缓存列车信息
     *
     * @param trainId
     * @return
     */
    @Override
    public TrainDO getCacheTrainByTrainId(String trainId) {
        return cache.safeGet(TRAIN_INFO_BY_TRAINID + trainId, ADVANCE_TICKET_DAY, TimeUnit.DAYS, () -> {
            return trainMapper.selectById(trainId);
        });
    }
}
