package org.wjx.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.wjx.Res;
import org.wjx.annotation.Idempotent;
import org.wjx.dto.req.PassengerRemoveReqDTO;
import org.wjx.dto.req.PassengerReqDTO;
import org.wjx.dto.resp.PassengerActualRespDTO;
import org.wjx.dto.resp.PassengerRespDTO;
import org.wjx.enums.IdempotentSceneEnum;
import org.wjx.enums.IdempotentTypeEnum;
import org.wjx.service.PassengerService;
import org.wjx.user.core.UserContext;

import java.util.List;

/**
 * @author xiu
 * @create 2023-11-21 19:38
 */
@RestController
@RequestMapping("/passenger")
@RequiredArgsConstructor
@Slf4j
public class PassengerController {
    private final PassengerService passengerService;
    @GetMapping("/query")
    public Res<List<PassengerRespDTO>> listPassengerQueryByUsername() {
        return Res.success(passengerService.listPassengerQueryByUsername(UserContext.getUserName()));
    }

    /**
     * 根据乘车人 ID 集合查询乘车人列表
     */
    @GetMapping("/inner/passenger/actual/query/ids")
    public Res<List<PassengerActualRespDTO>> listPassengerQueryByIds(@RequestParam("username") String username, @RequestParam("ids") List<Long> ids) {
        return Res.success(passengerService.listPassengerQueryByIds(username, ids));
    }
    @Idempotent(            prefix = "index12306-user:lock_passenger-alter:",
            key = "T(org.wjx.user.core.UserContext).getUserName()",
            type = IdempotentTypeEnum.SPEL,
            scene = IdempotentSceneEnum.RESTFUL,
            message = "正在保存乘车人，请稍后再试..."
    )
    @PostMapping("/passenger/save")
    public Res<Void> savePassenger(@RequestBody PassengerReqDTO requestParam) {
        passengerService.savePassenger(requestParam);
        return Res.success();
    }
    @Idempotent(            prefix = "index12306-user:lock_passenger-alter:",
            key = "T(org.wjx.user.core.UserContext).getUserName()",
            type = IdempotentTypeEnum.SPEL,
            scene = IdempotentSceneEnum.RESTFUL,
            message = "正在更新乘车人，请稍后再试..."
    )
    @PostMapping("/passenger/update")
    public Res<Void> updatePassenger(@RequestBody PassengerReqDTO requestParam) {
        passengerService.updatePassenger(requestParam);
        log.info("updatePassenger----------------------------");
        return Res.success();
    }
    @Idempotent(            prefix = "index12306-user:lock_passenger-alter:",
            key = "T(org.wjx.user.core.UserContext).getUserName()",
            type = IdempotentTypeEnum.SPEL,
            scene = IdempotentSceneEnum.RESTFUL,
            message = "正在删除乘车人，请稍后再试..."
    )
    @PostMapping("/passenger/remove")
    public Res<Void> removePassenger(@RequestBody PassengerRemoveReqDTO requestParam) {
        passengerService.removePassenger(requestParam);
        return Res.success();
    }
}
