package org.wjx.filter;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author xiu
 * @create 2023-12-07 10:53
 */
@Slf4j@Component
public class FeignRequestInterceptor implements RequestInterceptor {
    /**
     * 微服务之间传递的唯一标识
     */
    public static final String X_REQUEST_ID = "X-Request-Id";

    @Override
    public void apply(RequestTemplate template) {

        HttpServletRequest httpServletRequest = getHttpServletRequest();

        if (httpServletRequest != null) {
            //获取头信息
            Map<String, String> headers = getHeaders(httpServletRequest);
            // 传递所有请求头,防止部分丢失
            Iterator<Map.Entry<String, String>> iterator = headers.entrySet().iterator();

            //将请求的头信息放入到RequestTemplate 的头信息中，当使用RequestTemplate发起请求时会自动添加头信息
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                if (entry.getKey().equals("content-length"))continue;
                template.header(entry.getKey(), entry.getValue());
            }
            // 微服务之间传递的唯一标识,区分大小写所以通过httpServletRequest获取
            if (httpServletRequest.getHeader(X_REQUEST_ID) == null) {
                String sid = String.valueOf(UUID.randomUUID());
                template.header(X_REQUEST_ID, sid);
            }
            log.debug("FeignRequestInterceptor:{}", template.toString());
        }
    }

    /**
     * RequestContextHolder 中获取 HttpServletRequest对象
     *
     * @return
     */
    private HttpServletRequest getHttpServletRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取头信息
     *
     * @param request
     * @return
     */
    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> map = new LinkedHashMap<>();
        Enumeration<String> enumeration = request.getHeaderNames();
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                String key = enumeration.nextElement();
                String value = request.getHeader(key);
                map.put(key, value);
            }
        }
        return map;
    }
}
