package org.wjx.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author xiu
 * @create 2023-12-07 19:28
 */
@Data
public class TicketOrderCreateReqDTO {

    /**
     * 用户 ID
     */
    @NotBlank
    private Long userId;

    /**
     * 用户名
     */
    @NotBlank
    private String username;

    /**
     * 车次 ID
     */
    @NotBlank
    private Long trainId;

    /**
     * 出发站点
     */
    @NotBlank
    private String departure;

    /**
     * 到达站点
     */
    @NotBlank
    private String arrival;

    /**
     * 订单来源
     */
    @NotBlank
    private Integer source;

    /**
     * 下单时间
     */
    @NotBlank
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderTime;

    /**
     * 乘车日期
     */
    @NotBlank
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date ridingDate;

    /**
     * 列车车次
     */
    @NotBlank
    private String trainNumber;

    /**
     * 出发时间
     */
    @NotBlank
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date departureTime;

    /**
     * 到达时间
     */
    @NotBlank
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date arrivalTime;

    /**
     * 订单明细
     */
    @NotNull
    private List<TicketOrderItemCreateReqDTO> ticketOrderItems;
}