package org.wjx.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.wjx.dao.DO.TrainStationRelationDO;

import java.util.List;

/**
 * @author xiu
 * @create 2023-12-01 17:44
 */
public interface TrainStationRelationMapper extends BaseMapper<TrainStationRelationDO> {
    /**
     * 查询火车经过那些城市
     * @param startTime
     * @param startRegion
     * @param endRegion
     * @return
     */
    public List<TrainStationRelationDO> queryByParam(@Param("startTime") String startTime,
                                                     @Param("startRegion") String startRegion,
                                                     @Param("endRegion") String endRegion);


}
