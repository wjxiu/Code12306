package org.wjx.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.wjx.Exception.ExceptionHandler.GlobalExceptionHandler;
import org.wjx.filter.FeignRequestInterceptor;
import org.wjx.filter.TokenInterceptor;
import org.wjx.mybatisConfig.MyBatisConfigration;

/**
 * @author xiu
 * @create 2023-11-20 21:21
 */
@Configuration
@RequiredArgsConstructor
@Import({MyFeignClientConfiguration.class, CorsConfig.class,RequestLoggingConfig.class,MyFeignClientConfiguration.class, MyBatisConfigration.class})
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

    @Bean
    public FeignRequestInterceptor feignRequestInterceptor(){
        return new FeignRequestInterceptor();
    }
}
