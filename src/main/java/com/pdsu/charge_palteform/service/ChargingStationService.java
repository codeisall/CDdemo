package com.pdsu.charge_palteform.service;

import com.pdsu.charge_palteform.entity.ChargingConnector;
import com.pdsu.charge_palteform.entity.dto.PageResponse;
import com.pdsu.charge_palteform.entity.dto.StationDetailResponse;
import com.pdsu.charge_palteform.entity.dto.StationListResponse;
import com.pdsu.charge_palteform.entity.dto.StationQueryRequest;

import java.math.BigDecimal;
import java.util.List;

public interface ChargingStationService {
    /**
     * 分页查询附近充电站
     */
    PageResponse<StationListResponse> queryNearbyStations(StationQueryRequest request);

    /**
     * 获取充电站详情
     */
    StationDetailResponse getStationDetail(String stationId, BigDecimal latitude, BigDecimal longitude);

    /**
     * 获取充电站的所有充电桩
     */
    List<ChargingConnector> getStationConnectors(String stationId);

    /**
     * 根据充电桩ID获取充电桩信息
     */
    ChargingConnector getConnectorById(String connectorId);

    /**
     * 搜索充电站（关键词搜索）
     */
    PageResponse<StationListResponse> searchStations(StationQueryRequest request);
}
