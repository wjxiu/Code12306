package org.wjx.service.loader;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Component;
import org.wjx.core.SafeCache;
import org.wjx.dao.DO.CarriageDO;
import org.wjx.dao.DO.TrainStationDO;
import org.wjx.dao.mapper.CarrageMapper;
import org.wjx.dao.mapper.SeatMapper;
import org.wjx.dao.mapper.TrainStationMapper;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.wjx.constant.RedisKeyConstant.*;
import static org.wjx.constant.SystemConstant.ADVANCE_TICKET_DAY;

/**
 * todo 没写完,不想写
 * 加载列车剩下的座位
 * @author xiu
 * @create 2023-12-04 20:05
 */
@Component@RequiredArgsConstructor
public class SeatMarginCacheLoader {
    final CarrageMapper carrageMapper;
    final SafeCache cache;
    final TrainStationMapper trainStationMapper;
    final SeatMapper seatMapper;
    public Map<String, String> load(List<String> trainIds, Map<String, Set<Integer>> TrainIdToSeatTypeMap, List<String[]>startStationToEndStation) {
        HashOperations hashOperations = cache.getInstance().opsForHash();
        if (TrainIdToSeatTypeMap==null||trainIds==null||startStationToEndStation==null){
            for (String trainId : trainIds) {
                for (String[] twoStation : startStationToEndStation) {
                    List<TrainStationDO> trainStationDOS = cache.safeGetForList(TRAIN_INFO + trainId, TrainStationDO.class, ADVANCE_TICKET_DAY, TimeUnit.DAYS, () -> {
                        return trainStationMapper.queryBytrainId(trainId);
                    });
                    Set<Integer> set = TrainIdToSeatTypeMap.get(trainId);
                    for (Integer type : set) {
                        String stratstation=twoStation[0];
                        String endstation=twoStation[1];
                        Object o = hashOperations.get(REMAINTICKETOFSEAT_TRAIN + String.join("-", trainId, stratstation, endstation), type.toString());
                        if (o == null) {
                            Integer count = seatMapper.countByTrainIdAndSeatTypeAndArrivalAndDeparture(trainId, type, stratstation, endstation);
                            hashOperations.put(REMAINTICKETOFSEAT_TRAIN + String.join("-", trainId, stratstation, endstation), type.toString(), count.toString());
                        }
                    }
                }
            }
//            自己拼接
        }else{
//            直接用

        }
        return null;
    }

    ArrayList<String[]> geneListOfCache(List<TrainStationDO> arr) {
        ArrayList<String[]> res = new ArrayList<>();
        arr.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getSequence())));
        for (int i = 0; i < arr.size(); i++) {
            for (int j = i + 1; j < arr.size(); j++) {
                res.add(new String[]{arr.get(i).getDeparture(), arr.get(j).getDeparture()});
            }
        }
        return res;
    }
}
