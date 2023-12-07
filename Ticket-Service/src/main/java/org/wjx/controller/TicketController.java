package org.wjx.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.wjx.MyLog;
import org.wjx.Res;
import org.wjx.dao.DO.TrainStationDO;
import org.wjx.dao.mapper.TrainStationMapper;
import org.wjx.dto.req.CancelTicketOrderReqDTO;
import org.wjx.dto.req.PurchaseTicketReqDTO;
import org.wjx.dto.req.RefundTicketReqDTO;
import org.wjx.dto.req.TicketPageQueryReqDTO;
import org.wjx.dto.resp.*;
import org.wjx.service.TicketService;
import org.wjx.utils.BeanUtil;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author xiu
 * @create 2023-11-28 15:09
 */
@MyLog
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/ticket-service/ticket")
public class TicketController {
    private final TicketService ticketService;
    final TrainStationMapper trainStationMapper;
    @GetMapping("/query")
    public Res<TicketPageQueryRespDTO> pageListTicketQuery( @Validated @RequestBody  TicketPageQueryReqDTO requestParam) {
        return Res.success(ticketService.pageListTicketQueryV1(requestParam));
    }
    @GetMapping("/query/v2")
    public Res<TicketPageQueryRespDTO> pageListTicketQueryV2(@Validated TicketPageQueryReqDTO requestParam) {
        return Res.success(ticketService.pageListTicketQueryV2(requestParam));
    }
    @PostMapping("/purchase")
    public Res<TicketPurchaseRespDTO> purchaseTickets(@Validated @RequestBody PurchaseTicketReqDTO requestParam) {
        return Res.success(ticketService.purchaseTicketsV1(requestParam));
    }
    @PostMapping("/purchase/v2")
    public Res<TicketPurchaseRespDTO> purchaseTicketsV2(@Validated @RequestBody PurchaseTicketReqDTO requestParam) {
        return Res.success(ticketService.purchaseTicketsV2(requestParam));
    }
    @PostMapping("/cancel")
    public Res<Void> cancelTicketOrder(@Validated @RequestBody CancelTicketOrderReqDTO requestParam) {
        ticketService.cancelTicketOrder(requestParam);
        return Res.success();
    }
    @GetMapping("/payinfo/query")
    public Res<PayInfoRespDTO> getPayInfo(@NotBlank @RequestParam(value = "orderSn") String orderSn) {
        return Res.success(ticketService.getPayInfo(orderSn));
    }
    /**
     * 公共退款接口
     */
    @PostMapping("/api/ticket-service/ticket/refund")
    public Res<RefundTicketRespDTO> commonTicketRefund(@Validated @RequestBody RefundTicketReqDTO requestParam) {
        return Res.success(ticketService.commonTicketRefund(requestParam));
    }
    /**
     * 根据列车 ID 查询站点信息
     */
    @GetMapping("/api/ticket-service/train-station/query")
    public Res<List<TrainStationQueryRespDTO>> listTrainStationQuery(String trainId) {
        List<TrainStationDO> trainStationDOS = trainStationMapper.selectList(new LambdaQueryWrapper<TrainStationDO>().eq(TrainStationDO::getStationId, trainId));
        List<TrainStationQueryRespDTO> trainStationQueryRespDTOS = BeanUtil.convertToList(trainStationDOS, TrainStationQueryRespDTO.class);
        return Res.success(trainStationQueryRespDTOS);
    }
}
