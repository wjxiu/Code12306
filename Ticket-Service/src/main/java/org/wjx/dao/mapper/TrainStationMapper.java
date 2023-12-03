package org.wjx.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.wjx.dao.DO.TrainStationDO;

import java.util.List;

/**
 * @author xiu
 * @create 2023-11-29 16:19
 */
public interface TrainStationMapper extends BaseMapper<TrainStationDO> {
    public List<TrainStationDO> querystartRegionAndDepartureTime(@Param("startTime") String startTime,
                                                                         @Param("startRegion") String startRegion);
    List<TrainStationDO> queryBytrainIds(@Param("list") List<String> ids);

}
