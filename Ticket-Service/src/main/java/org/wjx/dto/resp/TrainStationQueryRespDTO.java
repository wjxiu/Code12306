
package org.wjx.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 列车站点查询响应参数
 *
 * @公众号：马丁玩编程，回复：加群，添加马哥微信（备注：12306）获取项目资料
 */
@Data
public class TrainStationQueryRespDTO {

    /**
     * 站序
     */
    private String sequence;

    /**
     * 站名
     */
    private String departure;

    /**
     * 到站时间
     */
    @JsonFormat(pattern = "HH:mm", timezone = "GMT+8")
    private Date arrivalTime;

    /**
     * 出发时间
     */
    @JsonFormat(pattern = "HH:mm", timezone = "GMT+8")
    private Date departureTime;

    /**
     * 停留时间
     */
    private Integer stopoverTime;
}
