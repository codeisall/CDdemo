package com.pdsu.charge_palteform.controller;

import com.pdsu.charge_palteform.common.Result;
import com.pdsu.charge_palteform.service.DataSyncService;
import com.pdsu.charge_palteform.service.EnergyPlatformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "数据同步管理", description = "与电能平台的数据同步接口")
@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class DataSyncController {
    private final DataSyncService dataSyncService;
    private final EnergyPlatformService energyPlatformService;

    @Operation(summary = "测试电能平台连接", description = "测试与电能平台的连接和Token获取")
    @PostMapping("/test_connection")
    public Result<String> testConnection() {
        try {
            String token = energyPlatformService.getAccessToken();
            return Result.success("电能平台连接成功，Token: " + token.substring(0, Math.min(20, token.length())) + "...");
        } catch (Exception e) {
            return Result.error("电能平台连接失败: " + e.getMessage());
        }
    }

    @Operation(summary = "同步充电站基础信息", description = "从电能平台同步充电站和充电桩的基础信息")
    @PostMapping("/station_info")
    public Result<String> syncStationInfo() {
        try {
            dataSyncService.syncStationInfo();
            return Result.success("充电站基础信息同步完成");
        } catch (Exception e) {
            return Result.error("同步失败: " + e.getMessage());
        }
    }

    @Operation(summary = "同步充电桩状态", description = "从电能平台同步充电桩的实时状态信息")
    @PostMapping("/connector_status")
    public Result<String> syncConnectorStatus() {
        try {
            dataSyncService.syncConnectorStatus();
            return Result.success("充电桩状态同步完成");
        } catch (Exception e) {
            return Result.error("同步失败: " + e.getMessage());
        }
    }

    @Operation(summary = "全量数据同步", description = "执行完整的数据同步，包括基础信息和状态信息")
    @PostMapping("/full_sync")
    public Result<String> fullSync() {
        try {
            dataSyncService.fullSync();
            return Result.success("全量数据同步完成");
        } catch (Exception e) {
            return Result.error("同步失败: " + e.getMessage());
        }
    }
}
