package org.wjx.service;

import org.wjx.dto.req.UserQueryActualRespDTO;
import org.wjx.dto.req.UserUpdateReqDTO;
import org.wjx.dto.resp.UserQueryRespDTO;

/**
 * @author xiu
 * @create 2023-11-21 19:12
 */
public interface UserService {
    UserQueryRespDTO queryUserByUsername(String username);

    UserQueryActualRespDTO queryActualUserByUsername(String username);


    /**
     * 根据用户 ID 修改用户信息
     *
     * @param requestParam 用户信息入参
     */
    void update(UserUpdateReqDTO requestParam);
}
