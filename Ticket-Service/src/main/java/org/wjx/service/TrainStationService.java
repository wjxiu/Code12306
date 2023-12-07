package org.wjx.service;

import org.wjx.dao.DO.RouteDTO;
import org.wjx.dto.resp.TrainStationQueryRespDTO;

import java.util.List;

/**
 * @author xiu
 * @create 2023-11-29 16:14
 */
public interface TrainStationService {
    List<TrainStationQueryRespDTO> listTrainStationQuery(String trainId);

    /**
     * 查询两个站点之间的经过的线路
     * @param trainId
     * @param departure
     * @param arrival
     * @return
     */
    List<RouteDTO> listTakeoutTrainStationRoute(String trainId, String departure, String arrival);

}
