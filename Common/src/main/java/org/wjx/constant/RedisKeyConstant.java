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
    public static final String STATION_ALL = "index12306-ticket-service:all_station";
    public static final String LOCK_QUERY_REGION_STATION_LIST = "index12306-ticket-service:lock:query_region_station_list_%s";
    public static final String REGION_STATION = "index12306-ticket-service:region-station:";

    /**
     * 用户注册可复用用户名分片，Key Prefix + Idx
     */
    public static final String USER_REGISTER_REUSE_SHARDING = "user-service:user-reuse:";
    /**
     * 列车和站点的映射
     */
    public static final String REGION_TRAIN_STATION_MAPPING = "ticket-service:region_train_station_mapping";

    /**
     * 用户乘车人列表，Key Prefix + 用户名
     */
    public static final String USER_PASSENGER_LIST = "user-service:user-passenger-list:";
}
