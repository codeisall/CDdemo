package com.pdsu.charge_palteform.entity.platefrom.station;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StationInfo {

    @JsonProperty("StationID")
    private String StationID;

    @JsonProperty("OperatorID")
    private String OperatorID;

    @JsonProperty("EquipmentOwnerID")
    private String EquipmentOwnerID;

    @JsonProperty("StationName")
    private String StationName;

    @JsonProperty("CountryCode")
    private String CountryCode;

    private String AreaCode;
    private String Address;
    private String StationTel;
    private String ServiceTel;
    private Integer StationType;
    private Integer StationStatus;
    private Integer ParkNums;
    private BigDecimal StationLng;
    private BigDecimal StationLat;
    private String SiteGuide;
    private Integer Construction;
    private List<String> Pictures;
    private String MatchCars;
    private String ParkInfo;
    private String BusineHours;
    private String ElectricityFee;
    private String ServiceFee;
    private String ParkFee;
    private Integer ParkDiscountType;
    private String Payment;
    private Integer SupportOrder;
    private String Remark;

    @JsonProperty("EquipmentInfos")
    private List<EquipmentInfo> EquipmentInfos;
}
