package org.wjx.dto.req;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

/**
 * 地区&站点查询请求入参
 * @author xiu
 * @create 2023-11-29 9:46
 */
@Data
public class RegionStationQueryReqDTO {

    /**
     * 查询方式
     */
    @Range(max = 5)
    private Integer queryType;

    /**
     * 名称
     */
    private String name;
}
