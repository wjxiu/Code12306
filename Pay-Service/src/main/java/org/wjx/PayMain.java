package org.wjx;

import com.sun.tools.javac.Main;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author xiu
 * @create 2023-11-20 10:15
 */
@EnableDiscoveryClient
@SpringBootApplication
public class PayMain {
    public static void main(String[] args) {
        SpringApplication.run(PayMain.class);
    }
}