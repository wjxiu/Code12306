package org.wjx.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.wjx.dao.DO.RegionDO;
import org.wjx.dao.DO.TrainStationDO;
import org.wjx.dao.DO.TrainStationRelationDO;
import org.wjx.dto.resp.TicketPageQueryRespDTO;
import org.wjx.dto.resp.TrainStaationQueryResp;

import java.util.List;

/**
 * @author xiu
 * @create 2023-12-01 17:44
 */
public interface StationRelationMapper extends BaseMapper<TrainStationRelationDO> {
    public List<TrainStationRelationDO> queryByParam(@Param("startTime") String startTime,
                                                     @Param("startRegion") String startRegion,
                                                     @Param("endRegion") String endRegion);


}
