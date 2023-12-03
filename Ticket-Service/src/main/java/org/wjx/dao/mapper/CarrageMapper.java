package org.wjx.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.wjx.dao.DO.CarriageDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xiu
 * @create 2023-12-03 14:58
 */
public interface CarrageMapper extends BaseMapper<CarriageDO> {
    // 使用 MyBatis 提供的方法，按 train_id 分组，统计每个分组中不同的 carriage_type 的数量
    @MapKey("train_id")
    List<Map<String, Object>> countCarriageTypeByTrainId(@Param("trainIdList") Collection<String> ids);
}
