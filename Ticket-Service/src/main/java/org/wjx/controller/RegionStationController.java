package org.wjx.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wjx.Res;
import org.wjx.dto.req.RegionStationQueryReqDTO;
import org.wjx.dto.resp.RegionStationQueryRespDTO;
import org.wjx.dto.resp.StationQueryRespDTO;
import org.wjx.service.RegionStationService;

import java.util.List;

/**
 * 车站站点查询接口
 * @author xiu
 * @create 2023-11-29 9:43
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/ticket-service")
public class RegionStationController {
    final RegionStationService regionStationService;
    final StringRedisTemplate redisTemplate;
    /**
     * 查询车站&城市的集合信息
     */
    @GetMapping("/region-station/query")
    public Res<List<RegionStationQueryRespDTO>> listRegionStation(@Validated RegionStationQueryReqDTO requestParam) {
        return Res.success(regionStationService.listRegionStation(requestParam));
    }

    /**
     * 查询车站站点集合信息
     */
    @GetMapping("/station/all")
    public Res<List<StationQueryRespDTO>> listAllStation() {
        return Res.success(regionStationService.listAllStation());
    }
}
