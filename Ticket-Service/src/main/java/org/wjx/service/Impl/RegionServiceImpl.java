package org.wjx.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wjx.core.SafeCache;
import org.wjx.dao.DO.RegionDO;
import org.wjx.dao.mapper.RegionMapper;
import org.wjx.service.RegionService;

import static org.wjx.constant.RedisKeyConstant.CODE_TRAIN_NAME;

/**
 * @author xiu
 * @create 2023-12-14 20:43
 */
@Service@RequiredArgsConstructor
public class RegionServiceImpl extends ServiceImpl<RegionMapper, RegionDO> implements RegionService {
    final SafeCache cache;
    final RegionMapper regionMapper;
    /**
     * @param code
     * @return
     */
    @Override
    public String selectCacheRegionNameByCode(String code) {
        return cache.SafeGetOfHash(CODE_TRAIN_NAME, code, () -> regionMapper.selectRegionNameByCode(code));
    }
}
