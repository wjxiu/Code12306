

package org.wjx.service;


import org.wjx.dto.UserDeletionReqDTO;
import org.wjx.dto.req.UserLoginReqDTO;
import org.wjx.dto.req.UserRegisterReqDTO;
import org.wjx.dto.resp.UserLoginRespDTO;
import org.wjx.dto.resp.UserRegisterRespDTO;


public interface UserLoginService {
    public UserLoginRespDTO login(UserLoginReqDTO loginReqDTO);
    public Boolean haveUserName(String userName);
    public UserRegisterRespDTO register(UserRegisterReqDTO reqDTO);

    void deletion(UserDeletionReqDTO requestParam);
}
