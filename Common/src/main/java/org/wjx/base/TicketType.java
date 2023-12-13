package org.wjx.base;

/**
 * @author xiu
 * @create 2023-12-13 17:25
 */
public enum TicketType {
    ADULT("成人票", 0),
    STUDENT("学生票", 1);

    private final String label;
    private final int value;

    TicketType(String label, int value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public int getValue() {
        return value;
    }
}
enum DiscountsType {
    ADULT("成人", 0),
    CHILD("儿童", 1),
    STUDENT("学生", 2),
    DISABLED_VETERAN("残疾军人", 3);

    private final String label;
    private final int value;

    DiscountsType(String label, int value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public int getValue() {
        return value;
    }
}
enum TrainTag {
    FOO("复", "0", "#f29c58"),
    ZHI("智", "1", "#7db08d"),
    JING("静", "2", "#64a0f6"),
    PU("铺", "3", "#5d9bf6");

    private final String label;
    private final String value;
    private final String color;

    TrainTag(String label, String value, String color) {
        this.label = label;
        this.value = value;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public String getColor() {
        return color;
    }
}

enum TicketStatus {
    WAITING_PAYMENT("待支付", 0),
    PAID("已支付", 10),
    ENTERED("已进站", 20),
    CANCELED("已取消", 30),
    REFUNDED("已退票", 40),
    CHANGED("已改签", 50);

    private final String label;
    private final int value;

    TicketStatus(String label, int value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public int getValue() {
        return value;
    }
}
enum IdCardType {
    CHINESE_RESIDENT_ID_CARD("中国居民身份证", 0);

    private final String label;
    private final int value;

    IdCardType(String label, int value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public int getValue() {
        return value;
    }
}

enum SeatClassType {
    BUSINESS("商务座", 0),
    FIRST_CLASS("一等座", 1),
    SECOND_CLASS("二等座", 2),
    SECOND_CLASS_CABIN("二等包座", 3),
    FIRST_CLASS_SLEEPER("一等卧", 4),
    SECOND_CLASS_SLEEPER("二等卧", 5),
    SOFT_SLEEPER("软卧", 6),
    HARD_SLEEPER("硬卧", 7),
    HARD_SEAT("硬座", 8),
    ADVANCED_SOFT_SLEEPER("高级软卧", 9),
    MOVABLE_SLEEPER("动卧", 10),
    SOFT_SEAT("软座", 11),
    SPECIAL_CLASS("特等座", 12),
    NO_SEAT("无座", 13),
    OTHER("其他", 14);

    private final String label;
    private final int code;

    SeatClassType(String label, int code) {
        this.label = label;
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public int getCode() {
        return code;
    }
}
enum TrainBrand {
    GC_HIGH_SPEED("GC-高铁城际", 0),
    D_HIGH_SPEED("D-动车", 1),
    Z_DIRECT("Z-直达", 2),
    T_EXPRESS("T-特快", 3),
    K_FAST("K-快速", 4),
    OTHER("其他", 5),
    FUXINGHAO("复兴号", 6),
    INTELLIGENT_HIGH_SPEED("智能动车组", 7);

    private final String label;
    private final int code;

    TrainBrand(String label, int code) {
        this.label = label;
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public int getCode() {
        return code;
    }
}

enum Bank {
    ALIPAY("支付宝", 0, "https://epay.12306.cn/pay/pages/web/images/bank_zfb.gif"),
    WECHAT("微信", 1, "https://epay.12306.cn/pay/pages/web/images/bank_wx.gif"),
    INDUSTRIAL_BANK("工商银行", 10, "https://epay.12306.cn/pay/pages/web/images/bank_gsyh2.gif"),
    AGRICULTURAL_BANK("农业银行", 9, "https://epay.12306.cn/pay/pages/web/images/bank_nyyh2.gif"),
    CHINA_BANK("中国银行", 8, "https://epay.12306.cn/pay/pages/web/images/bank_zgyh2.gif"),
    CONSTRUCTION_BANK("建设银行", 7, "https://epay.12306.cn/pay/pages/web/images/bank_jsyh2.gif"),
    MERCHANTS_BANK("招商银行", 6, "https://epay.12306.cn/pay/pages/web/images/bank_zsyh2.gif"),
    POSTAL_SAVINGS_BANK("邮储银行", 5, "https://epay.12306.cn/pay/pages/web/images/bank_ycyh.gif"),
    CHINA_UNIONPAY("中国银联", 4, "https://epay.12306.cn/pay/pages/web/images/bank_zgyl.gif"),
    CHINA_RAILWAY_CARD("中铁银通卡", 3, "https://epay.12306.cn/pay/pages/web/images/bank_ztytk.gif"),
    INTERNATIONAL_CARD("国际卡", 12, "https://epay.12306.cn/pay/pages/web/images/bank_wk.gif"),
    COMMERCIAL_BANK("交通银行", 13, "https://epay.12306.cn/pay/pages/web/images/bank_jtyh.png");

    private final String name;
    private final int value;
    private final String imageUrl;

    Bank(String name, int value, String imageUrl) {
        this.name = name;
        this.value = value;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

enum Region {
    CHINA("中国", "0");

    private final String label;
    private final String value;

    Region(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }
}

enum CheckStatus {
    APPROVED("通过", 0),
    NOT_APPROVED("未通过", 1);

    private final String label;
    private final int value;

    CheckStatus(String label, int value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public int getValue() {
        return value;
    }
}

