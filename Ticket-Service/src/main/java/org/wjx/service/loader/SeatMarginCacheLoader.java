package org.wjx.service.loader;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Component;
import org.wjx.core.SafeCache;
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
 *
 * @author xiu
 * @create 2023-12-04 20:05
 */
@Component
@RequiredArgsConstructor
public class SeatMarginCacheLoader {
    final CarrageMapper carrageMapper;
    final SafeCache cache;
    final TrainStationMapper trainStationMapper;
    final SeatMapper seatMapper;

//    /**
//     * 生成seattype->seatCount的map
//     * @param trainIds
//     * @param TrainIdToSeatTypeMap
//     * @return
//     */
//    public Map<Integer, Integer> load(List<String> trainIds, Map<String, Set<Integer>> TrainIdToSeatTypeMap, String startStation,String ToEndStation) {
//        HashMap<Integer, Integer> map = new HashMap<>();
//        for (String trainId : trainIds) {
//                Set<Integer> set = TrainIdToSeatTypeMap.get(trainId);
//                for (Integer type : set) {
//                    String key = REMAINTICKETOFSEAT_TRAIN + String.join("-", trainId, stratstation, endstation);
//                    Integer seatCount = cache.SafeGetOfHash(key, type, () -> {
//                        return seatMapper.countByTrainIdAndSeatTypeAndArrivalAndDeparture(trainId, type, stratstation, endstation);
//                    });
//                    map.put(type,seatCount);
//                }
//
//        }
//        return map;
//    }
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
