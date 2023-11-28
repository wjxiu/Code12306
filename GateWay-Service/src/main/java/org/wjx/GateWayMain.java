package org.wjx;

import com.sun.tools.javac.Main;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author xiu
 * @create 2023-11-20 10:16
 */
@EnableDiscoveryClient
@SpringBootApplication( exclude = {
        DataSourceAutoConfiguration.class, // 排除数据源的自动配置
})
public class GateWayMain {
    public static void main(String[] args) {
        SpringApplication.run(GateWayMain.class);
    }
}