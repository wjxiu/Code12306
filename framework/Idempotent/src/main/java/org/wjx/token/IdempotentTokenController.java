package org.wjx.token;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wjx.Res;

/**
 * @author xiu
 * @create 2023-11-27 14:02
 */
@RestController
@RequiredArgsConstructor
public class IdempotentTokenController {
    final IdempotentTokenExecuteHandler handler;
    @GetMapping("/createToken")
    public Res<String> createToken(){
      return Res.success(handler.createToken());
    }
}
