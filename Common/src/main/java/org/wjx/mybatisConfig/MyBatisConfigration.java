package org.wjx.mybatisConfig;

import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiu
 * @create 2023-12-14 10:25
 */
@Configuration
@MapperScan("org.wjx.dao") // 扫描你的 MyBatis Mapper 接口所在的包
public class MyBatisConfigration {
    @Bean
    public Interceptor myBatisTimingInterceptor() {
        return new MyBatisTimingInterceptor();
    }
}