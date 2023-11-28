package org.wjx.Exception;

import lombok.ToString;
import org.wjx.ErrorCode.BaseErrorCode;
import org.wjx.ErrorCode.IErrorCode;

/**
 * @author xiu
 * @create 2023-11-20 15:25
 */
@ToString
public class ServiceException extends AbstractException{

    public ServiceException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }
    public ServiceException(String message) {
        super(message, null, BaseErrorCode.SERVICE_ERROR);
    }
    public ServiceException (IErrorCode errorCode) {
        super(null, null, errorCode);
    }
    public ServiceException(String message, IErrorCode errorCode) {
        super(message, null, errorCode);
    }
}
