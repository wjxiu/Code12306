package org.wjx.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.wjx.dao.DO.TrainDO;
import org.wjx.dao.DO.TrainStationRelationDO;

import java.util.List;

/**
 * @author xiu
 * @create 2023-12-01 18:15
 */
public interface TrainMapper extends BaseMapper<TrainDO> {
    public List<TrainDO> queryByParam(@Param("departureTime") String departureTime,
                                      @Param("departRegion") String departRegion,
                                      @Param("arrivalRegion") String arrivalRegion
    );
    List<TrainDO> queryTodayTrain();

}
