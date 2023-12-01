package org.wjx.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.data.repository.query.Param;
import org.wjx.dao.DO.SeatDO;

import java.util.List;

/**
 * @author xiu
 * @create 2023-11-30 9:44
 */
public interface SeatMapper extends BaseMapper<SeatDO> {
    List<Integer> listSeatRemainingTicket(@Param("seatDO") SeatDO seatDO,@Param("trainCarriageList") List<String> trainCarriageList);
}
