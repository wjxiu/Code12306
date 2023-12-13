package org.wjx.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.wjx.Exception.ClientException;
import org.wjx.Exception.ServiceException;
import org.wjx.core.SafeCache;
import org.wjx.dao.entity.PassengerDO;
import org.wjx.dao.mapper.PassengerMapper;
import org.wjx.dto.req.PassengerRemoveReqDTO;
import org.wjx.dto.req.PassengerReqDTO;
import org.wjx.dto.resp.PassengerActualRespDTO;
import org.wjx.dto.resp.PassengerRespDTO;
import org.wjx.enums.VerifyStatusEnum;
import org.wjx.service.PassengerService;
import org.wjx.utils.BeanUtil;
import org.wjx.user.core.UserContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.wjx.constant.RedisKeyConstant.USER_PASSENGER_LIST;

/**
 * todo 缓存结果
 *
 * @author xiu
 * @create 2023-11-21 19:41
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {
    final PassengerMapper passengerMapper;
    final PlatformTransactionManager transactionManager;
    final SafeCache cache;


    @Override
    public List<PassengerRespDTO> listPassengerQueryByUsername(String username) {
        LambdaQueryWrapper<PassengerDO> queryWrapper = new LambdaQueryWrapper<PassengerDO>().eq(PassengerDO::getUsername, username);
        List<PassengerDO> passengerDOS = passengerMapper.selectList(queryWrapper);
        return BeanUtil.convertToList(passengerDOS, PassengerRespDTO.class);
    }

    @Override
    public List<PassengerActualRespDTO> listPassengerQueryByIds(String username, List<Long> ids) {
        ArrayList<PassengerActualRespDTO> res = new ArrayList<>();
        List<PassengerDO> list = cache.safeGetForList(USER_PASSENGER_LIST + UserContext.getUserName(), 30L, TimeUnit.DAYS, () -> {
            LambdaQueryWrapper<PassengerDO> queryWrapper = Wrappers.lambdaQuery(PassengerDO.class).eq(PassengerDO::getUsername, username);
            return passengerMapper.selectList(queryWrapper);
        });
       return  list.stream().filter(a-> ids.contains(a.getId())).map(a-> BeanUtil.convert(a,PassengerActualRespDTO.class)).toList();
    }

    @Override
    public void savePassenger(PassengerReqDTO requestParam) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        String userName = UserContext.getUserName();
        try {
            PassengerDO passdo = BeanUtil.convert(requestParam, PassengerDO.class);
            passdo.setUsername(userName);
            passdo.setCreateDate(new Date());
            passdo.setVerifyStatus(VerifyStatusEnum.REVIEWED.getCode());
            int insert = passengerMapper.insert(passdo);
            if (insert<=0)throw new ServiceException(String.format("[%s] 新增乘车人失败", userName));
            transactionManager.commit(status);
        } catch (RuntimeException e){
            transactionManager.rollback(status);
            throw new ClientException("生成passenger失败");
        }
//        todo 剩下一个删除缓存,不知道干嘛的
    }

    @Override
    public void updatePassenger(PassengerReqDTO requestParam) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        String userName = UserContext.getUserName();
        try {
            PassengerDO passdo = BeanUtil.convert(requestParam, PassengerDO.class);
            passdo.setUsername(userName);
            LambdaUpdateWrapper<PassengerDO> updateWrapper = Wrappers.lambdaUpdate(PassengerDO.class)
                    .eq(PassengerDO::getUsername, userName)
                    .eq(PassengerDO::getId, requestParam.getId());
            int update = passengerMapper.update(passdo, updateWrapper);
            if (update<=0)throw new ServiceException("更新乘客失败");
            transactionManager.commit(status);
        }catch ( ServiceException e){
            transactionManager.rollback(status);
            throw new ServiceException("更新乘客失败");
        }
    }

    @Override
    public void removePassenger(PassengerRemoveReqDTO requestParam) {
        String id = requestParam.getId();
        passengerMapper.deleteById(id);
    }
    @Override
    public void removePassengerBatch(List<PassengerRemoveReqDTO> requestParam) {
        List<String> list = requestParam.stream().map(PassengerRemoveReqDTO::getId).toList();
        passengerMapper.deleteBatchIds(list);
    }
}
