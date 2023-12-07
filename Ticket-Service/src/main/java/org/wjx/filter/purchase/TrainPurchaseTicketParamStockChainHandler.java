package org.wjx.filter.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.wjx.Exception.ClientException;
import org.wjx.core.SafeCache;
import org.wjx.dto.req.PurchaseTicketReqDTO;
import org.wjx.handler.DTO.PurchaseTicketPassengerDetailDTO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wjx.constant.RedisKeyConstant.REMAINTICKETOFSEAT_TRAIN;

/**
 * todo 暂时跳过
 *  座位数量信息加载暂时分离不出来
 * 购票流程过滤器之验证列车站点库存是否充足
 *
 * @author xiu
 * @create 2023-12-04 19:44
 */
@Component@RequiredArgsConstructor
public class TrainPurchaseTicketParamStockChainHandler implements TrainPurchaseTicketChainFilter<PurchaseTicketReqDTO>{
    final SafeCache cache;
    /**
     *
     *
     * @param requestParam 被过滤的数据
     */
    @Override
    public void handle(PurchaseTicketReqDTO requestParam) {
        List<String> chooseSeats = requestParam.getChooseSeats();
        if (!CollUtil.isEmpty(chooseSeats)){
            String trainId = requestParam.getTrainId();
            List<PurchaseTicketPassengerDetailDTO> passengerDetails = requestParam.getPassengers();
            Map<Integer, List<PurchaseTicketPassengerDetailDTO>> TypeToList = passengerDetails.stream().collect(Collectors.groupingBy(PurchaseTicketPassengerDetailDTO::getSeatType));
            String keySuffix = StrUtil.join("-", requestParam.getTrainId(), requestParam.getDeparture(), requestParam.getArrival());
            StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) cache.getInstance();
            HashOperations hashOperations = stringRedisTemplate.opsForHash();
            for (Map.Entry<Integer, List<PurchaseTicketPassengerDetailDTO>> entry : TypeToList.entrySet()) {
                Integer key = entry.getKey();
                List<PurchaseTicketPassengerDetailDTO> value = entry.getValue();
                Integer count =(Integer) hashOperations.get(REMAINTICKETOFSEAT_TRAIN+keySuffix, key);
                if (count==null){
//                    这里没有保存到座位数量信息,需要查询

                }
                if (count<=0)throw new ClientException("无余票");
            }
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
