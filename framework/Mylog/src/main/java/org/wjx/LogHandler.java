package org.wjx;

/**
 * @author xiu
 * @create 2023-11-20 10:20
 */
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@Aspect
public class LogHandler {
    @SneakyThrows
    @Around("@annotation(org.wjx.MyLog)||@within(org.wjx.MyLog)")
    public Object printLog(ProceedingJoinPoint joinPoint){
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String name = methodSignature.getName();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        String className = targetClass.getSimpleName();
        Method declaredMethod = targetClass.getDeclaredMethod(name, methodSignature.getMethod().getParameterTypes());
        MyLog logAnnotation = Optional.ofNullable(declaredMethod.getAnnotation(MyLog.class)).orElse(targetClass.getAnnotation(MyLog.class));
        Object proceed = null;
        long start = System.currentTimeMillis();
        try {
            proceed = joinPoint.proceed();
        }catch (Exception e){
//            doLog(joinPoint, logAnnotation, start, className, name, methodSignature);
            throw e;
        }finally {
            doLog(joinPoint, logAnnotation, start, className, name, methodSignature);
        }
        return proceed;
    }

    private void doLog(ProceedingJoinPoint joinPoint, MyLog logAnnotation, long start, String className, String name, MethodSignature methodSignature) {
        if (logAnnotation !=null){
            Long duration = System.currentTimeMillis() - start;
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            assert servletRequestAttributes != null;
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String requestURI = request.getRequestURI();
            Map<String, Object> paramKV = buildInput(joinPoint);
            String requestType = request.getMethod();
            log.info("方法: {} | 请求类型: {} | url: {} | 耗时: {}ms | 参数列表:[{}] | 参数详情:{}",
                    className +"#"+ name,
                    requestType,
                    requestURI,
                    duration,
                    buildParamNames(methodSignature),
                    sortParam(methodSignature,paramKV));
        }
    }

    //    生成kv，未排序的
    private Map<String, Object> buildInput(ProceedingJoinPoint joinPoint) {
        long start = System.currentTimeMillis();
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Map<String, Object> paramMap = new HashMap<>();
        if (parameterNames==null||parameterNames.length==0) return paramMap;
        for (int i = 0; i < parameterNames.length; i++) {
            if ((args[i] instanceof HttpServletRequest) || args[i] instanceof HttpServletResponse) continue;
            String paramName = parameterNames[i];
            Object paramValue = args[i];
            if (args[i] instanceof MultipartFile) {
//                paramName="MultipartFile";
                paramValue=((MultipartFile) args[i]).getOriginalFilename();
            }
            if (args[i] instanceof MultipartFile[] files){
//                paramName="MultipartFiles";
                paramValue = Arrays.stream(files).map(MultipartFile::getOriginalFilename).collect(Collectors.joining(","));
                paramValue="["+paramValue+"]";
            }
            paramMap.put(paramName, paramValue);
        }
        return paramMap;
    }
    //    获取方法所有形参名字 按照方法的参数顺序排序
    private String buildParamNames(MethodSignature methodSignature){
        String[] parameterNames = methodSignature.getParameterNames();
        if (parameterNames.length==0)return "EMPTY";
        Class[] parameterTypes = methodSignature.getParameterTypes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterTypes[i].isArray()){
                Class parameterType = parameterTypes[i];
                sb.append(parameterType.getComponentType().getSimpleName()+"[]").append(",");
            }else{
                sb.append(parameterTypes[i].getSimpleName()).append(" ").append(parameterNames[i]).append(",");
            }
        }
      return sb.substring(0,sb.length()-1);
    }
    //    参数数组 按照方法法的参数顺序排序
    private Map<String, Object>  sortParam(MethodSignature methodSignature,Map<String, Object> unsortedMap){
        String[] parameterNames = methodSignature.getParameterNames();
        LinkedHashMap<String, Object> sortedMap = new LinkedHashMap<>();
        for (String paramName : parameterNames) {
            if (unsortedMap.containsKey(paramName)) {
                sortedMap.put(paramName, unsortedMap.get(paramName));
            }
        }
        return sortedMap;
    }
}