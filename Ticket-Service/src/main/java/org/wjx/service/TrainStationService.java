package org.wjx.service;

import org.wjx.dto.resp.TrainStationQueryRespDTO;

import java.util.List;

/**
 * @author xiu
 * @create 2023-11-29 16:14
 */
public interface TrainStationService {
    List<TrainStationQueryRespDTO> listTrainStationQuery(String trainId);
}
