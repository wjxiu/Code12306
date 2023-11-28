package org.wjx.Exception;

import lombok.ToString;
import org.wjx.ErrorCode.BaseErrorCode;
import org.wjx.ErrorCode.IErrorCode;

/**
 * @author xiu
 * @create 2023-11-20 15:25
 */
@ToString
public class RemoteException extends AbstractException{
    public RemoteException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }
    public RemoteException(String message) {
        super(message, null, BaseErrorCode.REMOTE_ERROR);
    }
    public RemoteException(  IErrorCode errorCode) {
        super(null, null, errorCode);
    }
    public RemoteException(String message,  IErrorCode errorCode) {
        super(message, null, errorCode);
    }
}
