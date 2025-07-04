package com.pdsu.charge_palteform.entity.platefrom;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class PlatformRequest {

    @JsonProperty("OperatorID")
    private String OperatorID;

    @JsonProperty("Data")
    private String Data; // 加密后的数据

    @JsonProperty("TimeStamp")
    private String TimeStamp;

    @JsonProperty("Seq")
    private String Seq;

    @JsonProperty("Sig")
    private String Sig; // 签名
}
