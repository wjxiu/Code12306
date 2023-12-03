package org.wjx.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.wjx.dao.DO.RegionDO;

/**
 * @author xiu
 * @create 2023-12-03 20:38
 */
public interface RegionMapper extends BaseMapper<RegionDO> {
    String selectRegionNameByCode(String code);

}
