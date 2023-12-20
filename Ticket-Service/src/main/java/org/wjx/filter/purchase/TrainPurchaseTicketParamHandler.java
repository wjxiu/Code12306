package org.wjx.filter.purchase;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wjx.Exception.ClientException;
import org.wjx.dao.DO.TrainDO;
import org.wjx.dao.mapper.RegionMapper;
import org.wjx.dao.mapper.StationMapper;
import org.wjx.dao.mapper.TrainMapper;
import org.wjx.dto.req.PurchaseTicketReqDTO;
import org.wjx.handler.DTO.PurchaseTicketPassengerDetailDTO;

import java.util.*;

/**
 * 校验车票的代码是否正确
 * @author xiu
 * @create 2023-12-11 20:43
 */
@Component@RequiredArgsConstructor
public class TrainPurchaseTicketParamHandler implements TrainPurchaseTicketChainFilter<PurchaseTicketReqDTO>{
    final RedissonClient redissonClient;
    final RegionMapper regionMapper;
    final StationMapper stationMapper;
    final TrainMapper trainMapper;
    /**
     * 定义过滤逻辑
     *
     * @param reqParam 被过滤的数据
     */
    @Override
    public void handle(PurchaseTicketReqDTO reqParam) {
        if (reqParam.getChooseSeats()==null||reqParam.getChooseSeats().isEmpty())return;
        HashMap<Integer,Integer> map=new HashMap<>();
        map.put(0,3);
        map.put(1,4);
        map.put(2,5);
        for (int i = 0; i < reqParam.getPassengers().size(); i++) {
            PurchaseTicketPassengerDetailDTO pass = reqParam.getPassengers().get(i);
            String s = reqParam.getChooseSeats().get(i);
            Integer i1 = map.get(pass.getSeatType());
            if (Character.toLowerCase(Character.toLowerCase(s.charAt(0))-'a')>i1)throw new ClientException("座位代号出错");
        }
        RBloomFilter<Object> TrainIdNotNullBloomFilter = redissonClient.getBloomFilter("TrainIdNotNull");
//        if (!TrainIdNotNullBloomFilter.contains(reqParam.getTrainId())) throw new ClientException("列车不存在");
    }
    @Override
    public String mark() {

        return "TrainPurchaseTicketChainFilter";
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void initTrainIdNotNullbloomfilter(){
        RBloomFilter<Object> TrainIdNotNullBloomFilter = redissonClient.getBloomFilter("TrainIdNotNull");
        for (TrainDO trainDO : trainMapper.queryTodayTrain()) {
            TrainIdNotNullBloomFilter.add(trainDO.getId());
        }
    }
    /**
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
