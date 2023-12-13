package org.wjx.constant;

/**
 * @author xiu
 * @create 2023-11-20 16:59
 */
public class RedisKeyConstant {
    /**
     * 用户注册锁，Key Prefix + 用户名
     */
    public static final String LOCK_USER_REGISTER = "user-service:lock:user-register:";

    /**
     * 用户注销锁，Key Prefix + 用户名
     */
    public static final String USER_DELETION = "user-service:user-deletion:";
    /**
     * 全部站点的缓存key
     */
    public static final String STATION_ALL = "index12306-ticket-service:all_station:";
    public static final String LOCK_QUERY_REGION_STATION_LIST = "index12306-ticket-service:lock:query_region_station_list_%s";
    public static final String REGION_STATION = "index12306-ticket-service:region-station:";

    /**
     * 列车和站点的映射
     */
    public static final String REGION_TRAIN_STATION_MAPPING = "ticket-service:region_train_station_mapping:";

    /**
     * 用户乘车人列表，Key Prefix + 用户名
     */
    public static final String USER_PASSENGER_LIST = "user-service:user-passenger-list:";
    /**
     * 列车对应的站点之间的座位数
     */
    public static final String REMAINTICKETOFSEAT_TRAIN="ticket-service:train-seat-count:";
    /**
     * 列车经过的站点
     */
    public static final String TRAIN_PASS_ALL_STATION ="ticket-service:train-pass-all-station:";
    /**
     * 保存列车的座位type集合
     */
    public static final String TRAINCARRAGE ="ticket-sevice:train:carriage:";
    public static final String LOCK_PURCHASE_TICKETS = "ticket-service:lock:purchase_tickets_%s";

    /**
     * 火车代码映射火车名字
     */
    public static final String CODE_TRAIN_NAME= "ticket:sevice:code:trainName:";
    /**
     * 查询火车经过那些城市
     */
    public static final String TRAIN_PASS_ALL_CITY = "ticket:sevice:train-pass-all-city:";
    /**
     *通过火车id查询火车信息
     */
    public static final String TRAIN_INFO_BY_TRAINID= "ticket:sevice:train-info-trainId:";
    /**
     *通过火车id查询火车信息
     */
    public static final String TRAIN_PRICE_HASH = "ticket:sevice:train-price:";




}
