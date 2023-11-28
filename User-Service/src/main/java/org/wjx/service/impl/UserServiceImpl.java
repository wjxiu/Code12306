package org.wjx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wjx.Exception.ClientException;
import org.wjx.dao.entity.UserDO;
import org.wjx.dao.mapper.UserMapper;
import org.wjx.dto.req.UserQueryActualRespDTO;
import org.wjx.dto.req.UserUpdateReqDTO;
import org.wjx.dto.resp.UserQueryRespDTO;
import org.wjx.service.UserLoginService;
import org.wjx.service.UserService;
import org.wjx.toolkit.BeanUtil;

/**
 * @author xiu
 * @create 2023-11-21 19:12
 */
@Service@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    UserMapper userMapper;
    @Override
    public UserQueryRespDTO queryUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = userMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException("用户不存在，请检查用户名是否正确");
        }
        return BeanUtil.convert(userDO, UserQueryRespDTO.class);
    }

    @Override
    public UserQueryActualRespDTO queryActualUserByUsername(String username) {
        return BeanUtil.convert(queryUserByUsername(username), UserQueryActualRespDTO.class);
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
        UserDO userDO = BeanUtil.convert(requestParam, UserDO.class);
        LambdaUpdateWrapper<UserDO> userUpdateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());
        userMapper.update(userDO, userUpdateWrapper);
    }
}

