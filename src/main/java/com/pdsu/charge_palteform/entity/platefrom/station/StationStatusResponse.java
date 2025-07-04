package com.pdsu.charge_palteform.entity.platefrom.station;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class StationStatusResponse {

    @JsonProperty("StationStatusInfos")
    private List<StationStatusInfo> StationStatusInfos;
}
