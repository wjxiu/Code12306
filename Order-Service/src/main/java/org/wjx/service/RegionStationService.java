package org.wjx.service;

import org.wjx.dto.req.RegionStationQueryReqDTO;
import org.wjx.dto.resp.RegionStationQueryRespDTO;
import org.wjx.dto.resp.StationQueryRespDTO;

import java.util.List;

/**
 * @author xiu
 * @create 2023-11-29 9:49
 */
public interface RegionStationService {
    List<RegionStationQueryRespDTO> listRegionStation(RegionStationQueryReqDTO requestParam);

    List<StationQueryRespDTO> listAllStation();
}
