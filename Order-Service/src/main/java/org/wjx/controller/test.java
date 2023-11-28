package org.wjx.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wjx.MyLog;
import org.wjx.controller.feign.payfeign;

/**
 * @author xiu
 * @create 2023-11-20 10:49
 */

@MyLog
@RestController
public class test {
    @Autowired
    payfeign payfeign;
    @GetMapping()
    public void test(int a,int b,int c){
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
    }
    @GetMapping("/feign")
    public void test1(){
        payfeign.getaa();
    }
}
