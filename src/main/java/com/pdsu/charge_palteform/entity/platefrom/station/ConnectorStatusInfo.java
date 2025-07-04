package com.pdsu.charge_palteform.entity.platefrom.station;

import lombok.Data;

@Data
public class ConnectorStatusInfo {
    private String ConnectorID;
    private Integer Status;
    private Integer ParkStatus;
    private Integer LockStatus;
}
