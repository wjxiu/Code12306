package org.wjx.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiu
 * @create 2023-11-20 13:05
 */
@RestController("")
public class paycontroller {
    @GetMapping("/abs")
    public int  getaa(){
        return 1;
    }
}
