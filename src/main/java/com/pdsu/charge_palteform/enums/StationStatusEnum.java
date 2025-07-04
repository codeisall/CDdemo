package com.pdsu.charge_palteform.enums;

import lombok.Getter;

@Getter
public enum StationStatusEnum {
    UNKNOWN(0, "未知"),
    BUILDING(1, "建设中"),
    OPERATING(2, "运营中"),
    OFFLINE(3, "关闭下线");

    private final Integer code;
    private final String desc;

    StationStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDesc(Integer code) {
        for (StationStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return "未知";
    }
}
