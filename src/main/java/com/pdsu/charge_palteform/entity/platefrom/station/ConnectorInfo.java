package com.pdsu.charge_palteform.entity.platefrom.station;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConnectorInfo {

    private String ConnectorID;
    private String ConnectorName;
    private Integer ConnectorType;
    private Integer VoltageUpperLimits;
    private Integer VoltageLowerLimits;
    private Integer Current;
    private BigDecimal Power;
    private String ParkNo;
    private Integer NationalStandard;

}
