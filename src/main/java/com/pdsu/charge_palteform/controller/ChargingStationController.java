package com.pdsu.charge_palteform.controller;

import com.pdsu.charge_palteform.common.Result;
import com.pdsu.charge_palteform.entity.ChargingConnector;
import com.pdsu.charge_palteform.entity.dto.PageResponse;
import com.pdsu.charge_palteform.entity.dto.StationDetailResponse;
import com.pdsu.charge_palteform.entity.dto.StationListResponse;
import com.pdsu.charge_palteform.entity.dto.StationQueryRequest;
import com.pdsu.charge_palteform.service.ChargingStationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@Tag(name = "充电站管理", description = "充电站查询相关接口")
@RestController
@RequestMapping("/api/station")
@RequiredArgsConstructor
public class ChargingStationController {

    private final ChargingStationService chargingStationService;

    @Operation(summary = "查询附近充电站", description = "根据位置信息查询附近的充电站")
    @PostMapping("/nearby")
    public Result<PageResponse<StationListResponse>> queryNearbyStations(
            @Valid @RequestBody StationQueryRequest request) {
        PageResponse<StationListResponse> response = chargingStationService.queryNearbyStations(request);
        return Result.success(response);
    }

    @Operation(summary = "搜索充电站", description = "根据关键词搜索充电站")
    @PostMapping("/search")
    public Result<PageResponse<StationListResponse>> searchStations(
            @Valid @RequestBody StationQueryRequest request) {
        PageResponse<StationListResponse> response = chargingStationService.searchStations(request);
        return Result.success(response);
    }

    @Operation(summary = "获取充电站详情", description = "根据充电站ID获取详细信息")
    @GetMapping("/detail/{stationId}")
    public Result<StationDetailResponse> getStationDetail(
            @Parameter(description = "充电站ID") @PathVariable String stationId,
            @Parameter(description = "用户纬度") @RequestParam(required = false) BigDecimal latitude,
            @Parameter(description = "用户经度") @RequestParam(required = false) BigDecimal longitude) {
        StationDetailResponse response = chargingStationService.getStationDetail(stationId, latitude, longitude);
        return Result.success(response);
    }

    @Operation(summary = "获取充电站的充电桩列表", description = "获取指定充电站下的所有充电桩")
    @GetMapping("/{stationId}/connectors")
    public Result<List<ChargingConnector>> getStationConnectors(
            @Parameter(description = "充电站ID") @PathVariable String stationId) {
        List<ChargingConnector> connectors = chargingStationService.getStationConnectors(stationId);
        return Result.success(connectors);
    }

    @Operation(summary = "获取充电桩详情", description = "根据充电桩ID获取详细信息")
    @GetMapping("/connector/{connectorId}")
    public Result<ChargingConnector> getConnectorDetail(
            @Parameter(description = "充电桩ID") @PathVariable String connectorId) {
        ChargingConnector connector = chargingStationService.getConnectorById(connectorId);
        return Result.success(connector);
    }
}
