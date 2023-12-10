package org.wjx;

import com.sun.tools.javac.Main;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author xiu
 * @create 2023-11-20 10:15
 */
@EnableDiscoveryClient
@MapperScan(basePackages = "org.wjx.dao.mapper")
@SpringBootApplication
public class PayMain {
    public static void main(String[] args) {
        SpringApplication.run(PayMain.class);
    }
}