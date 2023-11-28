package org.wjx.user.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xiu
 * @create 2023-11-20 16:05
 */
@Builder@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserInfoDTO {
    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 用户 Token
     */
    private String token;
}
