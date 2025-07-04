package com.pdsu.charge_palteform.entity.platefrom;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlatformResponse {

    @JsonProperty("Ret")
    private Integer Ret;

    @JsonProperty("Msg")
    private String Msg;

    @JsonProperty("Data")
    private String Data; // 加密的响应数据

    @JsonProperty("Sig")
    private String Sig; // 签名
}
