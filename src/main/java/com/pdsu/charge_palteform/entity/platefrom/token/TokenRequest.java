package com.pdsu.charge_palteform.entity.platefrom.token;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TokenRequest {

    @JsonProperty("OperatorID")
    private String OperatorID;

    @JsonProperty("OperatorSecret")
    private String OperatorSecret;
}
