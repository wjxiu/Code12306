package org.wjx.dto.resp;

import lombok.Data;

/**
 * @author xiu
 * @create 2023-11-20 16:51
 */
@Data
public class UserRegisterRespDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;
}
