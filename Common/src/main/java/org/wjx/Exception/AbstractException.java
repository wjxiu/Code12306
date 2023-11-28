package org.wjx.Exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;
import org.wjx.ErrorCode.IErrorCode;

import java.util.Optional;

/**
 * 抽象项目中三类异常体系，客户端异常、服务端异常以及远程服务调用异常
 * @see  ClientException
 * @see  ServiceException
 * @see  RemoteException
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AbstractException extends RuntimeException {
    public final String errorCode;

    public final String errorMessage;

    public AbstractException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable);
        this.errorCode = errorCode.code();
        this.errorMessage = Optional.ofNullable(StringUtils.hasLength(message) ? message : null).orElse(errorCode.message());
    }
}
