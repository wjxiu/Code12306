package org.wjx.dao.DO;

import lombok.Data;

/**
 * 列车线路,每两个站点之间
 * @author xiu
 * @create 2023-12-06 10:21
 */
@Data
public class RouteDTO {
    String startStation;
    String endStation;
    public RouteDTO(String startStation, String endStation) {
        this.startStation = startStation;
        this.endStation = endStation;
    }
}
