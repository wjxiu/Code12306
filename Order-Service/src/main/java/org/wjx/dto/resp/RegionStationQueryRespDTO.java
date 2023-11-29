package org.wjx.dto.resp;

import lombok.Data;

/**
 * 地区&站点分页查询响应参
 * @author xiu
 * @create 2023-11-29 9:46
 */
@Data
public class RegionStationQueryRespDTO {

    /**
     * 名称
     */
    private String name;

    /**
     * 地区编码
     */
    private String code;

    /**
     * 拼音
     */
    private String spell;
}
