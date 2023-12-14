
package org.wjx.dao.DO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.wjx.base.BaseDO;

/**
 * 列车站点价格实体
 *
 * @公众号：马丁玩编程，回复：加群，添加马哥微信（备注：12306）获取项目资料
 */
@Data
@TableName("t_train_station_price")
public class TrainStationPriceDO extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 车次id
     */
    private Long trainId;

    /**
     * 座位类型
     */
    private Integer seatType;

    /**
     * 出发站点
     */
    private String departure;

    /**
     * 到达站点
     */
    private String arrival;

    /**
     * 车票价格
     */
    private Integer price;
}
