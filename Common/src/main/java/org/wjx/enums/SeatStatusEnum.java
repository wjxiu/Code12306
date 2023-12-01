package org.wjx.enums;

/**
 * @author xiu
 * @create 2023-11-30 9:51
 */
public enum SeatStatusEnum {
    AVAILABLE(0),

    /**
     * 锁定
     */
    LOCKED(1),

    /**
     * 已售
     */
    SOLD(2);

    SeatStatusEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

  private   Integer code;
}
