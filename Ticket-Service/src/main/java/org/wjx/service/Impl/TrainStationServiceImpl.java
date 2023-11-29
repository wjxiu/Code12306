package org.wjx.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wjx.dao.mapper.StationMapper;
import org.wjx.dao.mapper.TrainStationMapper;
import org.wjx.dto.entiey.TrainStationDO;
import org.wjx.dto.resp.TrainStationQueryRespDTO;
import org.wjx.service.TrainStationService;
import org.wjx.toolkit.BeanUtil;

import java.util.List;

/**
 * @author xiu
 * @create 2023-11-29 16:14
 */
@Service@RequiredArgsConstructor
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
}
