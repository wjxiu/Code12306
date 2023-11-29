package org.wjx.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wjx.Res;
import org.wjx.dto.resp.TrainStationQueryRespDTO;
import org.wjx.service.TrainStationService;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author xiu
 * @create 2023-11-29 16:14
 */
@RestController
@Validated
@RequiredArgsConstructor
public class TrainStationController {
    private final TrainStationService trainStationService;

    /**
     * 根据列车 ID 查询站点信息
     */
    @GetMapping("/api/ticket-service/train-station/query")
    public Res<List<TrainStationQueryRespDTO>> listTrainStationQuery(@NotBlank String trainId) {
        return Res.success(trainStationService.listTrainStationQuery(trainId));
    }
}
