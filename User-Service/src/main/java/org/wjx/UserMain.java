package org.wjx;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;


/**
 * @author xiu
 * @create 2023-11-20 10:14
 */
@MapperScan(basePackages = {"org.wjx.dao"})
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"org.wjx"})
public class UserMain {
    public static void main(String[] args) {
        SpringApplication.run(UserMain.class);
    }
}
