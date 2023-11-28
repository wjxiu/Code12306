package org.wjx.core;

import lombok.extern.slf4j.Slf4j;
import org.wjx.param.IdempotentParamService;
import org.wjx.spel.IdempotentSPELService;
import org.wjx.spel.IdempotentSpELByMQExecuteHandler;
import org.wjx.token.IdempotentTokenService;
import org.wjx.enums.IdempotentSceneEnum;
import org.wjx.enums.IdempotentTypeEnum;
import org.wjx.user.core.ApplicationContextHolder;

/**
 * 用于创造IdempotentExecuteHandler
 * @author xiu
 * @create 2023-11-24 19:05
 */
@Slf4j
public class IdempotentExecuteHandlerFactory {

    static IdempotentExecuteHandler getBean(IdempotentSceneEnum scene, IdempotentTypeEnum type) {
        IdempotentExecuteHandler handler = null;
        switch (scene) {
            case RESTFUL -> {
                switch (type) {
                    case TOKEN -> {
                        handler= ApplicationContextHolder.getBean(IdempotentTokenService.class);
                    }
                    case SPEL -> {
                        handler= ApplicationContextHolder.getBean(IdempotentSPELService.class);
                    }
                    case PARAM -> {
                        handler= ApplicationContextHolder.getBean(IdempotentParamService.class);
                    }
                }
            }
            case MQ -> {
                handler= ApplicationContextHolder.getBean(IdempotentSpELByMQExecuteHandler.class);
            }
        }
        return handler;
    }
}
