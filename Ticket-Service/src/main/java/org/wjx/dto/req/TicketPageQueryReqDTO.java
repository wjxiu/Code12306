package org.wjx.dto.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import org.wjx.base.PageRequest;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import java.util.Date;

/**
 * 车票分页查询请求参数
 * @author xiu
 * @create 2023-11-28 15:14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TicketPageQueryReqDTO extends PageRequest {
    /**
     * 出发地 Code
     */
    @NotBlank
    private String fromStation;

    /**
     * 目的地 Code
     */
    @NotBlank
    private String toStation;

    /**
     * 出发日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
//    @FutureOrPresent(message = "出发日期过早")
    private Date departureDate;


    /**
     * 出发站点
     */

    private String departure;

    /**
     * 到达站点
     */

    private String arrival;
}
