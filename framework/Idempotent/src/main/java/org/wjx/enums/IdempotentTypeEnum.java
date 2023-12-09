package org.wjx.enums;

/**
 * @author xiu
 * @create 2023-11-24 18:26
 */
public enum IdempotentTypeEnum {
    /**
     * 对于PARAM验证来说,参数不能非空,用参数的md5值作为key
     */
    PARAM,
    TOKEN,SPEL;
}
