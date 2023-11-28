package org.wjx.Exception.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.wjx.ErrorCode.BaseErrorCode;
import org.wjx.Exception.AbstractException;
import org.wjx.Exception.ClientException;
import org.wjx.Res;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author xiu
 * @create 2023-11-20 21:13
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = {AbstractException.class, ClientException.class})
    public Object handleValidatedException(HttpServletRequest request, AbstractException ex) {
        String errorMessage = ex.getErrorMessage();
        String exceptionStr = Optional.ofNullable(errorMessage)
                .orElse("EMPTY");
        log.error("[{}] {} [ex] {}", request.getMethod(), getUrl(request), exceptionStr);
        return Res.failure(exceptionStr, BaseErrorCode.CLIENT_ERROR.code());
    }

    @ExceptionHandler(value = {BindException.class, MethodArgumentNotValidException.class})
    public Object BindException(Exception e) {
        if (e instanceof MethodArgumentNotValidException ex0) {
            return ex0.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, f -> {
                return Optional.ofNullable(f.getDefaultMessage()).orElse("参数校验失败");
            }));
        }
        if (e instanceof BindException ex) {
            return ex.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, f -> {
                return Optional.ofNullable(f.getDefaultMessage()).orElse("参数校验失败");
            }));
        }
        return "unknown";
    }
    @ExceptionHandler(value = Throwable.class)
    public Res defaultErrorHandler(HttpServletRequest request, Throwable throwable) {
        log.error("[{}] {} ", request.getMethod(), getUrl(request), throwable);
        return Res.failure();
    }
    private String getUrl(HttpServletRequest request) {
        if (StringUtils.isEmpty(request.getQueryString())) {
            return request.getRequestURL().toString();
        }
        return request.getRequestURL().toString() + "?" + request.getQueryString();
    }
}
