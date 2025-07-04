package com.pdsu.charge_palteform.entity.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StationDetailResponse {
    private String stationId;
    private String stationName;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String stationTel;
    private BigDecimal serviceFee;
    private String parkingFee;
    private String openingHours;
    private Integer stationStatus;
    private String statusText; // 状态文本描述

    // 距离信息
    private BigDecimal distance; // 距离（公里）

    // 充电桩统计信息
    private Integer totalConnectors; // 总充电桩数
    private Integer availableConnectors; // 可用充电桩数
    private Integer chargingConnectors; // 充电中充电桩数
    private Integer faultConnectors; // 故障充电桩数

    // 充电桩详细信息
    private List<ConnectorDetail> connectors;

    @Data
    public static class ConnectorDetail {
        private String connectorId;
        private String connectorName;
        private Integer connectorType;
        private String connectorTypeText; // 类型文本描述
        private BigDecimal ratedPower;
        private BigDecimal currentPower;
        private BigDecimal electricityFee;
        private BigDecimal serviceFee;
        private Integer status;
        private String statusText; // 状态文本描述
    }
}
