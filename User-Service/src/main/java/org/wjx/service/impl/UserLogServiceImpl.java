package org.wjx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.redisson.RedissonBloomFilter;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.wjx.Exception.ClientException;
import org.wjx.Exception.ServiceException;
import org.wjx.FilterChain.UserRegisterAbstractFilter;
import org.wjx.constant.RedisKeyConstant;
import org.wjx.dao.entity.UserDO;
import org.wjx.dao.mapper.UserLogMapper;
import org.wjx.dao.mapper.UserMapper;
import org.wjx.dto.UserDeletionReqDTO;
import org.wjx.dto.req.PassengerRemoveReqDTO;
import org.wjx.dto.req.UserLoginReqDTO;
import org.wjx.dto.req.UserRegisterReqDTO;
import org.wjx.dto.resp.PassengerRespDTO;
import org.wjx.dto.resp.UserLoginRespDTO;
import org.wjx.dto.resp.UserRegisterRespDTO;
import org.wjx.enums.UserChainMarkEnum;
import org.wjx.filter.AbstractFilter;
import org.wjx.filter.AbstractFilterChainsContext;
import org.wjx.service.PassengerService;
import org.wjx.service.UserLoginService;
import org.wjx.toolkit.BeanUtil;
import org.wjx.toolkit.JWTUtil;
import org.wjx.user.core.UserContext;
import org.wjx.user.core.UserInfoDTO;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author xiu
 * @create 2023-11-20 15:40
 */
@RequiredArgsConstructor
@Service
public class UserLogServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserLoginService {
    final RedissonClient redissonClient;
    final UserLogMapper userLogMapper;
    final UserMapper userMapper;
    final RBloomFilter<String> usernameFilter;
    final AbstractFilterChainsContext abstractFilterContext;
    final PassengerService passengerService;
    String LOCK = "UserLogServiceImpl::removeUser::";

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO loginReqDTO) {
        UserLoginRespDTO login = userLogMapper.login(loginReqDTO);
        if (login == null) throw new ClientException("用户名或密码不正确,请检查输入");
        UserInfoDTO build = UserInfoDTO.builder().userId(login.getUserId()).realName(login.getRealName()).username(login.getUsername()).build();
        String token = JWTUtil.generateAccessToken(build);
        build.setToken(token);
        login.setAccessToken(token);
//        todo 放入缓存中或者theadlocal中
        UserContext.set(build);

        return login;
    }

    public void logout(String accessToken) {
        if (StringUtils.hasLength(accessToken)) {
//            distributedCache.delete(accessToken);
        }
    }

    public Boolean haveUserName(String userName) {
        boolean contains = usernameFilter.contains(userName);
        if (!contains) return false;
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getUsername, userName);
        return count(wrapper) > 0;
    }

    public UserRegisterRespDTO register(UserRegisterReqDTO reqDTO) {
        abstractFilterContext.execute(UserRegisterAbstractFilter.class, reqDTO);
        RLock lock = redissonClient.getLock(RedisKeyConstant.LOCK_USER_REGISTER);
        boolean locksuccess = lock.tryLock();
        if (!locksuccess) throw new ServiceException("用户名已存在");
        try {
            UserDO convert = BeanUtil.convert(reqDTO, UserDO.class);
            LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserDO::getIdCard, reqDTO.getIdCard()).eq(UserDO::getIdType, reqDTO.getIdType());
            if (count(wrapper) >= 1) throw new ServiceException("证件重复");
//        -----------------
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserDO::getPhone, reqDTO.getPhone());
            if (count(wrapper) >= 1) throw new ServiceException("手机号重复");
//        ---------------------
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserDO::getMail, reqDTO.getMail());
            if (count(wrapper) >= 1) throw new ServiceException("邮箱重复");
            int insert = userMapper.insert(convert);
            if (insert < 1) throw new ServiceException("注册失败");
            usernameFilter.add(reqDTO.getUsername());
        } finally {
            lock.unlock();
        }
        return BeanUtil.convert(reqDTO, UserRegisterRespDTO.class);
    }

    @Override
    public void deletion(UserDeletionReqDTO requestParam) {
        String loginuserName = UserContext.getUserName();
        String userId = UserContext.getUserId();
        if (!Objects.equals(requestParam.getUsername(), loginuserName)) throw new ClientException("禁止注销他人账号");
        RLock lock = redissonClient.getLock(LOCK + loginuserName);
        boolean b = lock.tryLock();
        try {
            if (b) {
                List<PassengerRespDTO> passengerRespDTOS = passengerService.listPassengerQueryByUsername(loginuserName);
                List<PassengerRemoveReqDTO> collect = passengerRespDTOS.stream().map((pass -> {
                    PassengerRemoveReqDTO removeReqDTO = new PassengerRemoveReqDTO();
                    removeReqDTO.setId(pass.getId());
                    return removeReqDTO;
                })).toList();
//        注销用户对应的乘车人
                passengerService.removePassengerBatch(collect);
                UserDO userDO = userMapper.selectById(userId);
//        注销用户
                LambdaQueryWrapper<UserDO> eq = new LambdaQueryWrapper<UserDO>()
                        .eq(UserDO::getId, userId)
                        .eq(UserDO::getIdCard, userDO.getIdCard())
                        .eq(UserDO::getIdType, userDO.getIdType());
                userMapper.delete(eq);
            }
        } finally {
            lock.unlock();
        }

    }
}
