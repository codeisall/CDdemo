package com.pdsu.charge_palteform.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StationListResponse {
    private String stationId;
    private String stationName;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String stationTel;
    private BigDecimal serviceFee;
    private String openingHours;
    private Integer stationStatus;
    private String statusText;

    // 距离信息
    private BigDecimal distance; // 距离（公里）

    // 充电桩统计信息
    private Integer totalConnectors;
    private Integer availableConnectors;
    private Integer chargingConnectors;
    private Integer faultConnectors;

    // 价格范围
    private BigDecimal minElectricityFee;
    private BigDecimal maxElectricityFee;
}
