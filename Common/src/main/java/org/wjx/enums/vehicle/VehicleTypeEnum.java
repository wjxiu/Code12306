package org.wjx.enums.vehicle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

import static org.wjx.enums.vehicle.VehicleSeatTypeEnum.*;

/**
 * @author xiu
 * @create 2023-12-06 13:05
 */
@RequiredArgsConstructor
public enum VehicleTypeEnum {

    /**
     * 高铁
     */
    HIGH_SPEED_RAIN(0, "HIGH_SPEED_RAIN", "高铁", Arrays.asList(BUSINESS_CLASS.getCode(), FIRST_CLASS.getCode(), SECOND_CLASS.getCode())),

    /**
     * 动车
     */
    BULLET(1, "BULLET", "动车", Arrays.asList(SECOND_CLASS_CABIN_SEAT.getCode(), FIRST_SLEEPER.getCode(), SECOND_SLEEPER.getCode(), NO_SEAT_SLEEPER.getCode())),

    /**
     * 普通车
     */
    REGULAR_TRAIN(2, "REGULAR_TRAIN", "普通车", Arrays.asList(SOFT_SLEEPER.getCode(), HARD_SLEEPER.getCode(), HARD_SEAT.getCode(), NO_SEAT_SLEEPER.getCode()));
    @Getter
    private final Integer code;

    @Getter
    private final String name;

    @Getter
    private final String value;

    @Getter
    private final List<Integer> seatTypes;


    public static String findCNameByCode(Integer code){
        return Arrays.stream(VehicleTypeEnum.values())
                .filter(a -> a.getCode().equals(code)).findFirst()
                .map(VehicleTypeEnum::getName)
                .orElse(null);
    }
}
