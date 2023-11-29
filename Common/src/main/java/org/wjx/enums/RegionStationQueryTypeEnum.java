package org.wjx.enums;

import lombok.Data;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author xiu
 * @create 2023-11-29 10:46
 */
public enum RegionStationQueryTypeEnum {
    HOT(0,null),
    A_E(1,Arrays.asList("A","B","C","D","E")),
    F_J(2, Arrays.asList("F", "G", "H", "R", "J")),
    K_O(3, Arrays.asList("K", "L", "M", "N", "O")),
    P_T(4, Arrays.asList("P", "Q", "R", "S", "T")),

    /**
     * U to Z
     */
    U_Z(5, Arrays.asList("U", "V", "W", "X", "Y", "Z"));


    /**
     * 类型
     */
    private final Integer type;

    /**
     * 拼音列表
     */
    private final List<String> spells;

    RegionStationQueryTypeEnum(Integer type, List<String> spells) {
        this.type = type;
        this.spells = spells;
    }
}
