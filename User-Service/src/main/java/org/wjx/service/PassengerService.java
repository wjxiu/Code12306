package org.wjx.service;

import org.wjx.dto.req.PassengerRemoveReqDTO;
import org.wjx.dto.req.PassengerReqDTO;
import org.wjx.dto.resp.PassengerActualRespDTO;
import org.wjx.dto.resp.PassengerRespDTO;

import java.util.List;

/**
 * @author xiu
 * @create 2023-11-21 19:41
 */
public interface PassengerService {
    List<PassengerRespDTO> listPassengerQueryByUsername(String userName);

    List<PassengerActualRespDTO> listPassengerQueryByIds(String username, List<Long> ids);

    void savePassenger(PassengerReqDTO requestParam);

    void updatePassenger(PassengerReqDTO requestParam);

    void removePassenger(PassengerRemoveReqDTO requestParam);
    public void removePassengerBatch(List<PassengerRemoveReqDTO> requestParam);
}
