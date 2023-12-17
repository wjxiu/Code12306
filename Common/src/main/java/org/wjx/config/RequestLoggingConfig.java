package org.wjx.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * @author xiu
 * @create 2023-12-08 11:41
 */
public class RequestLoggingConfig {

//    @Bean
    public FilterRegistrationBean<CommonsRequestLoggingFilter> loggingFilter() {
        FilterRegistrationBean<CommonsRequestLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(10000);
        loggingFilter.setIncludeHeaders(false);
        loggingFilter.setAfterMessagePrefix("REQUEST DATA : ");
        registrationBean.setFilter(loggingFilter);
        registrationBean.addUrlPatterns("/*"); // 设置过滤路径，这里是所有路径
        return registrationBean;
    }
}
