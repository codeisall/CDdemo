package com.pdsu.charge_palteform.entity.platefrom.station;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class StationQueryResponse {

//    private Integer PageNo;
//    private Integer PageCount;
//    private Integer ItemSize;
//    private List<StationInfo> StationInfos;

    @JsonProperty("Total")
    private Integer total;

    @JsonProperty("StationInfos")
    private List<StationInfo> stationInfos;

}
