package org.wjx.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.wjx.Res;
import org.wjx.remote.dto.ResetSeatDTO;

import java.util.List;

/**
 * @author xiu
 * @create 2023-12-10 16:10
 */
@FeignClient("ticket-service")
public interface SeatRemoteService {
    @PostMapping("/api/ticket-service/ticket/ResetSeatStatus")
     Res<Boolean> ResetSeatStatus(@RequestBody List<ResetSeatDTO> list);
}
