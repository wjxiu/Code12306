package org.wjx.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.wjx.Exception.ExceptionHandler.GlobalExceptionHandler;
import org.wjx.filter.TokenInterceptor;

/**
 * @author xiu
 * @create 2023-11-20 21:21
 */
@Configuration
@RequiredArgsConstructor
public class WebAutoConfiguration  implements WebMvcConfigurer {
    final TokenInterceptor tokenInterceptor;
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor);
    }
}
