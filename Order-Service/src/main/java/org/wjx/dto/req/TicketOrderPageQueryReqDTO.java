
package org.wjx.dto.req;

import lombok.Data;
import org.wjx.page.PageRequest;

/**
 * 车票订单分页查询
 *
 * @公众号：马丁玩编程，回复：加群，添加马哥微信（备注：12306）获取项目资料
 */
@Data
public class TicketOrderPageQueryReqDTO extends PageRequest {

    /**
     * 用户唯一标识
     */
    private String userId;

    /**
     * 状态类型 0：未完成 1：未出行 2：历史订单
     */
    private Integer statusType;
}
