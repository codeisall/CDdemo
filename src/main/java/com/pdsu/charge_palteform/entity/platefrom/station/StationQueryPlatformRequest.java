package com.pdsu.charge_palteform.entity.platefrom.station;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StationQueryPlatformRequest {

    @JsonProperty("LastQueryTime")
    private String LastQueryTime; // 上次查询时间

    @JsonProperty("PageNo")
    private Integer PageNo;

    @JsonProperty("PageSize")
    private Integer PageSize;
}
