package org.wjx.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author xiu
 * @create 2023-12-01 19:47
 */
@Data@NoArgsConstructor@AllArgsConstructor
public class TrainStaationQueryResp {
    private Long id;
    private String trainNumber;
    private Integer trainType;
    private String trainTag;
    private String trainBrand;
    private String startStation;
    private String endStation;
    private String startRegion;
    private String endRegion;
    private Date saleTime;
    private Integer saleStatus;
    private Date departureTime;
    private Date arrivalTime;
    private String sequence;
    private Integer stopoverTime;

}
