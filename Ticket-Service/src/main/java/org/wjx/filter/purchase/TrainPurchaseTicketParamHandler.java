package org.wjx.filter.purchase;

import org.springframework.stereotype.Component;
import org.wjx.Exception.ClientException;
import org.wjx.dto.req.PurchaseTicketReqDTO;
import org.wjx.handler.DTO.PurchaseTicketPassengerDetailDTO;

import java.util.HashMap;
import java.util.List;

/**
 * 校验车票的代码是否正确
 * @author xiu
 * @create 2023-12-11 20:43
 */
@Component
public class TrainPurchaseTicketParamHandler implements TrainPurchaseTicketChainFilter<PurchaseTicketReqDTO>{
    /**
     * 定义过滤逻辑
     *
     * @param reqParam 被过滤的数据
     */
    @Override
    public void handle(PurchaseTicketReqDTO reqParam) {
        if (reqParam.getChooseSeats().isEmpty())return;
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
    }
    @Override
    public String mark() {

        return "TrainPurchaseTicketChainFilter";
    }

    /**
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
