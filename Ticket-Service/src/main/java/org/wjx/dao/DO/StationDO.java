package org.wjx.dao.DO;

/**
 * @author xiu
 * @create 2023-11-29 9:54
 */

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.wjx.base.BaseDO;
@Data
@TableName("t_station")
public class StationDO extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 车站编码
     */
    private String code;

    /**
     * 车站名称
     */
    private String name;

    /**
     * 拼音
     */
    private String spell;

    /**
     * 地区编号
     */
    private String region;

    /**
     * 地区名称
     */
    private String regionName;
}

