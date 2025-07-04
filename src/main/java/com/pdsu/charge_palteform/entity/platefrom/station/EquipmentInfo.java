package com.pdsu.charge_palteform.entity.platefrom.station;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class EquipmentInfo {
    private String EquipmentID;
    private String ManufacturerID;
    private String EquipmentModel;
    private String ProductionDate;
    private Integer EquipmentType;
    private List<ConnectorInfo> ConnectorInfos;
    private BigDecimal EquipmentLng;
    private BigDecimal EquipmentLat;
    private BigDecimal Power;
    private String EquipmentName;
}
