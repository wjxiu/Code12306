package org.wjx;

import lombok.Data;
import lombok.experimental.Accessors;
import org.wjx.ErrorCode.BaseErrorCode;
import org.wjx.Exception.AbstractException;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;

/**
 * @author xiu
 * @create 2023-11-20 15:15
 */
@Data
@Accessors(chain = true)
public class Res<T> implements Serializable {


    /**
     * 构造成功响应
     */
    public static Res<Void> success() {
        return new Res<Void>()
                .setCode(Res.SUCCESS_CODE);
    }

    /**
     * 构造带返回数据的成功响应
     */
    public static <T> Res<T> success(T data){
        return new Res<T>().setData(data).setCode(Res.SUCCESS_CODE);
    }

    /**
     * 构建服务端失败响应
     */
    public static Res<Void> failure() {
        return new Res<Void>()
                .setCode(BaseErrorCode.SERVICE_ERROR.code())
                .setMessage(BaseErrorCode.SERVICE_ERROR.message());
    }

    /**
     * 通过 {@link AbstractException} 构建失败响应
     */
    protected static Res<Void> failure(AbstractException abstractException) {
        String errorCode = Optional.ofNullable(abstractException.getErrorCode())
                .orElse(BaseErrorCode.SERVICE_ERROR.code());
        String errorMessage = Optional.ofNullable(abstractException.getErrorMessage())
                .orElse(BaseErrorCode.SERVICE_ERROR.message());
        return new Res<Void>()
                .setCode(errorCode)
                .setMessage(errorMessage);
    }

    /**
     * 通过 errorCode、errorMessage 构建失败响应
     */
     public static Res<Void> failure(String errorMessage,String errorCode) {
        return new Res<Void>()
                .setCode(errorCode)
                .setMessage(errorMessage);
    }


    @Serial
    private static final long serialVersionUID = 5679018624309023727L;

    /**
     * 正确返回码
     */
    public static final String SUCCESS_CODE = "0";

    /**
     * 返回码
     */
    private String code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 请求ID
     */
    private String requestId;

    public boolean isSuccess() {
        return SUCCESS_CODE.equals(code);
    }
}
