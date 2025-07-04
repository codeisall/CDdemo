package com.pdsu.charge_palteform.enums;

import lombok.Getter;

@Getter
public enum ConnectorTypeEnum {
    DC(1, "直流"),
    AC(2, "交流");

    private final Integer code;
    private final String desc;

    ConnectorTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDesc(Integer code) {
        for (ConnectorTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type.getDesc();
            }
        }
        return "未知";
    }
}
