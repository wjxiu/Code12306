package org.wjx.service;

/**
 * @author xiu
 * @create 2023-12-14 20:43
 */
public interface RegionService{
    /**
     * 通过城市代码找到城市名字
     * 并且缓存
     * @param code
     * @return
     */
    String selectCacheRegionNameByCode(String code);
}
