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
          routeDTOS.add(new RouteDTO(stations.get(startindex), stations.get(i)));
        }
        return routeDTOS;
    }

    /**
     * 根据起点，终点和线路找出被干扰的区间
     * one:找出不包含起点的
     * 将起点前和起点后的使用单向连接
     * two:找到除了终点以外的选择的节点
     * 将这些节点的和后面的单向连接
     * @return
     */
    public static List<RouteDTO> calEffectRoute(List<String> stations,String startStation, String endStation){
        int startindex = stations.indexOf(startStation);
        int endindex = stations.indexOf(endStation);
        ArrayList<RouteDTO> routeDTOS = new ArrayList<>();
        if (!(startindex!=-1&&endindex!=-1&&endindex>startindex)){
            return routeDTOS;
        }
        List<String> front = stations.subList(0, startindex);
        List<String> end = stations.subList(startindex+1, stations.size());
//        one:找出不包含起点的 将起点前和起点后的使用单向连接
        for (String fr : front) {
            for (String e : end) {
                routeDTOS.add(new RouteDTO(fr, e));
            }
        }
        for (int i = startindex; i < endindex; i++) {
            for (int j = i+1; j <stations.size(); j++) {
                if (i==j)continue;
                routeDTOS.add(new RouteDTO(stations.get(i), stations.get(j)));
            }
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
            for (int j = i+1; j <=endindex; j++) {
                if (i==j)continue;
                routeDTOS.add(new RouteDTO(stations.get(i), stations.get(j)));
            }
        }
        for (RouteDTO routeDTO : routeDTOS) {
            System.out.println(routeDTO.getStartStation()+"-"+routeDTO.getEndStation());
        }
        return routeDTOS;
    }

    public static void main(String[] args) {
        List<String> stations = Arrays.asList("A", "B", "C","D","E","F","G");
        String startStation = "C";
        String endStation = "G";
//        System.out.println(stations);
//        System.out.println(calculateDeepStation(stations, startStation, endStation));
        calEffectRoute(stations,startStation,endStation);
//        System.out.println(calculateStation(stations, startStation, endStation));
    }
}
