package com.pdsu.charge_palteform.entity.platefrom.station;

import lombok.Data;

import java.util.List;

@Data
public class StationStatusInfo {
    private List<ConnectorStatusInfo> StationStatusInfos;
}
