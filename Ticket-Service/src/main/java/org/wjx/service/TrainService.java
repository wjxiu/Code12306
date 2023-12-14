package org.wjx.service;

import org.wjx.dao.DO.TrainDO;

/**
 * @author xiu
 * @create 2023-12-14 20:49
 */
public interface TrainService {
    /**
     * 根据列车id获取列车并缓存列车信息
     * @param trainId
     * @return
     */
    TrainDO getCacheTrainByTrainId(String trainId);
}
