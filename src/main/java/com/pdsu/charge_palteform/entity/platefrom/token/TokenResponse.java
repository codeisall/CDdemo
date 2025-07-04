package com.pdsu.charge_palteform.entity.platefrom.token;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TokenResponse {
    //private String OperatorID;

    @JsonProperty("SuccStat")
    private Integer SuccStat;

    @JsonProperty("AccessToken")
    private String AccessToken;

    @JsonProperty("TokenAvailableTime")
    private Integer TokenAvailableTime;

    @JsonProperty("FailReason")
    private Integer FailReason;
}
