package org.wjx.utils;

import lombok.extern.slf4j.Slf4j;
import org.wjx.Exception.ClientException;
import org.wjx.dao.DO.RouteDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 计算站点的中间站点
 *
 * @author xiu
 * @create 2023-12-06 10:40
 */
@Slf4j
public class StationCalculateUtil {

    /**
     * 不详细计算,只计算第一层
     *[A, B, C]
     * [RouteDTO(A,B),RouteDTO(A,C)]
     * @param stations 列车线路的站点
     * @param startStation 起点
     * @param endStation 终点
     * @return 存在返回list<RouteDTO>,否则返回空list
     */
    public static List<RouteDTO> calculateStation(List<String> stations, String startStation, String endStation) {
        int startindex = stations.indexOf(startStation);
        int endindex = stations.indexOf(endStation);
        ArrayList<RouteDTO> routeDTOS = new ArrayList<>();
        if (!(startindex!=-1&&endindex!=-1&&endindex>startindex)) return routeDTOS;
        for (int i = startindex+1; i < stations.size(); i++) {
            if (!Objects.equals(stations.get(i), endStation)) routeDTOS.add(new RouteDTO(stations.get(startindex), stations.get(i)));
        }
        return routeDTOS;
    }
    /**
     * 详细计算,计算每一层
     *[A, B, C]
     * [RouteDTO(A,B),RouteDTO(A,C),RouteDTO(B,C)]
     * @param stations 列车线路的站点
     * @param startStation 起点
     * @param endStation 终点
     * @return 存在返回list<RouteDTO>,否则返回空list
     */
    public static List<RouteDTO> calculateDeepStation(List<String> stations, String startStation, String endStation) {
        int startindex = stations.indexOf(startStation);
        int endindex=stations.indexOf(endStation);
        ArrayList<RouteDTO> routeDTOS = new ArrayList<>();
        if (!(startindex!=-1&&endindex!=-1&&endindex>startindex)){
            return routeDTOS;
        }
        for (int i = startindex; i < endindex; i++) {
            for (int j = startindex+1; j <=endindex; j++) {
                if (i==j)continue;
                routeDTOS.add(new RouteDTO(stations.get(i), stations.get(j)));
            }
        }
        log.info("转换结果:{}",routeDTOS);
        return routeDTOS;
    }

    public static void main(String[] args) {
        List<String> stations = Arrays.asList("A", "B", "C");
        String startStation = "A";
        String endStation = "C";
        System.out.println(stations);
        System.out.println(calculateDeepStation(stations, startStation, endStation));
        System.out.println(calculateStation(stations, startStation, endStation));
    }
}
