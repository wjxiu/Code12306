package org.wjx.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.wjx.dao.DO.RouteDTO;
import org.wjx.dao.DO.TrainStationDO;
import org.wjx.dao.mapper.TrainStationMapper;

import org.wjx.dto.resp.TrainStationQueryRespDTO;
import org.wjx.service.TrainStationService;
import org.wjx.utils.BeanUtil;
import org.wjx.utils.StationCalculateUtil;

import java.util.Comparator;
import java.util.List;

/**
 * @author xiu
 * @create 2023-11-29 16:14
 */
@Service@Slf4j
@RequiredArgsConstructor
public class TrainStationServiceImpl implements TrainStationService {
    private final TrainStationMapper trainStationMapper;
    /**
     * @param trainId
     * @return
     */
    @Override
    public List<TrainStationQueryRespDTO> listTrainStationQuery(String trainId) {
        LambdaQueryWrapper<TrainStationDO> queryWrapper = Wrappers.lambdaQuery(TrainStationDO.class)
                .eq(TrainStationDO::getTrainId, trainId);
        List<TrainStationDO> trainStationDOList = trainStationMapper.selectList(queryWrapper);
        return BeanUtil.convertToList(trainStationDOList, TrainStationQueryRespDTO.class);
    }

    /**
     * 查询两个站点之间的需要扣票的的所有站点
     *
     * @param trainId 列车id
     * @param departure 出发站
     * @param arrival 到达站
     * @return 存在返回List<RouteDTO>，否则返回空list
     */
    @Override
    public List<RouteDTO> listTakeoutTrainStationRoute(String trainId, String departure, String arrival) {

        LambdaQueryWrapper<TrainStationDO> eq = new LambdaQueryWrapper<TrainStationDO>().eq(TrainStationDO::getTrainId, trainId)
                .select(TrainStationDO::getDeparture,TrainStationDO::getSequence);
        List<TrainStationDO> trainStationDOS = trainStationMapper.selectList(eq);
        List<String> list = trainStationDOS.stream().sorted(Comparator.comparing(TrainStationDO::getSequence)).map(TrainStationDO::getDeparture).toList();
        log.info("列车线路查询结果:{}",list);
        return StationCalculateUtil.calculateDeepStation(list, departure, arrival);
    }
}
