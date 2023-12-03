package org.wjx.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.data.repository.query.Param;
import org.wjx.dao.DO.RegionDO;
import org.wjx.dao.DO.StationDO;
import org.wjx.dao.DO.TrainStationRelationDO;

import java.util.List;

/**
 * @author xiu
 * @create 2023-11-29 9:53
 */
public interface StationMapper extends BaseMapper<StationDO> {
    List<StationDO>  GetlistRegionStationByFuzzyNameOrFuzzySpell(String name);
    List<RegionDO> GetlistRegionByType(@Param("type") Integer type);

}
