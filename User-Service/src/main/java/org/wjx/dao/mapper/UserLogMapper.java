package org.wjx.dao.mapper;

import org.wjx.dao.entity.UserDO;
import org.wjx.dto.req.UserLoginReqDTO;
import org.wjx.dto.req.UserRegisterReqDTO;
import org.wjx.dto.resp.UserLoginRespDTO;

/**
 * @author xiu
 * @create 2023-11-20 15:45
 */
public interface UserLogMapper {
    UserLoginRespDTO login(UserLoginReqDTO reqDTO);
//    UserDO register(UserRegisterReqDTO registerDTO);
}
