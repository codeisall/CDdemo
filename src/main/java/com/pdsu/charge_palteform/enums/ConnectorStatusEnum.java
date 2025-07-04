package com.pdsu.charge_palteform.enums;

import lombok.Getter;

@Getter
public enum ConnectorStatusEnum {
    OFFLINE(0, "离网"),
    IDLE(1, "空闲"),
    OCCUPIED_CHARGING(2, "占用（充电中）"),
    OCCUPIED_NOT_CHARGING(3, "占用（未充电）"),
    RESERVED(4, "预约"),
    FAULT(255, "故障");

    private final Integer code;
    private final String desc;

    ConnectorStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDesc(Integer code) {
        for (ConnectorStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return "未知";
    }
}
