package com.pdsu.charge_palteform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pdsu.charge_palteform.entity.ChargingConnector;
import com.pdsu.charge_palteform.entity.ChargingStation;
import com.pdsu.charge_palteform.entity.platefrom.station.ConnectorInfo;
import com.pdsu.charge_palteform.entity.platefrom.station.ConnectorStatusInfo;
import com.pdsu.charge_palteform.entity.platefrom.station.EquipmentInfo;
import com.pdsu.charge_palteform.entity.platefrom.station.StationInfo;
import com.pdsu.charge_palteform.mapper.ChargingConnectorMapper;
import com.pdsu.charge_palteform.mapper.ChargingStationMapper;
import com.pdsu.charge_palteform.service.DataSyncService;
import com.pdsu.charge_palteform.service.EnergyPlatformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncServiceImpl implements DataSyncService {

    private final EnergyPlatformService energyPlatformService;
    private final ChargingStationMapper stationMapper;
    private final ChargingConnectorMapper connectorMapper;

    @Override
    @Transactional
    public void syncStationInfo() {
        log.info("开始同步充电站基础信息...");
        try {
            int pageNo = 1;
            int pageSize = 100;
            boolean hasMore = true;
            int totalSynced = 0;
            while (hasMore) {
                // 从电能平台获取充电站信息
                List<StationInfo> stationInfos = energyPlatformService.queryStationsInfo(
                        null, pageNo, pageSize);
                if (CollectionUtils.isEmpty(stationInfos)) {
                    hasMore = false;
                    break;
                }
                // 处理每个充电站
                for (StationInfo stationInfo : stationInfos) {
                    syncSingleStation(stationInfo);
                    totalSynced++;
                }
                // 如果返回的数据少于pageSize，说明已经是最后一页
                if (stationInfos.size() < pageSize) {
                    hasMore = false;
                }
                pageNo++;
                log.info("已同步第{}页，本页{}个充电站", pageNo - 1, stationInfos.size());
            }
            log.info("充电站基础信息同步完成，共同步{}个充电站", totalSynced);
        } catch (Exception e) {
            log.error("同步充电站基础信息失败", e);
            throw new RuntimeException("同步充电站信息失败", e);
        }
    }

    @Override
    @Transactional
    public void syncConnectorStatus() {
        log.info("开始同步充电桩状态信息...");
        try {
            // 获取所有充电站ID
            List<ChargingStation> stations = stationMapper.selectList(null);
            List<String> stationIds = stations.stream()
                    .map(ChargingStation::getStationId)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(stationIds)) {
                log.warn("没有找到充电站，请先同步充电站基础信息");
                return;
            }
            // 分批查询状态（每次最多50个）
            int batchSize = 50;
            for (int i = 0; i < stationIds.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, stationIds.size());
                List<String> batchIds = stationIds.subList(i, endIndex);
                // 查询这批充电站的状态
                var statusInfos = energyPlatformService.queryStationStatus(batchIds);
                // 更新充电桩状态
                for (var statusInfo : statusInfos) {
                    updateConnectorStatus(statusInfo.getStationStatusInfos());
                }
                log.info("已同步第{}批充电桩状态，共{}个充电站", (i / batchSize) + 1, batchIds.size());
            }

            log.info("充电桩状态同步完成");

        } catch (Exception e) {
            log.error("同步充电桩状态失败", e);
            throw new RuntimeException("同步充电桩状态失败", e);
        }
    }

    @Override
    public void fullSync() {
        log.info("开始全量数据同步...");
        // 先同步基础信息，再同步状态信息
        syncStationInfo();
        syncConnectorStatus();
        log.info("全量数据同步完成");
    }

    /**
     * 同步单个充电站信息
     */
    private void syncSingleStation(StationInfo stationInfo) {
        try {
            // 1. 同步充电站基础信息
            ChargingStation station = convertToChargingStation(stationInfo);

            // 查询是否已存在
            ChargingStation existingStation = stationMapper.selectOne(
                    new LambdaQueryWrapper<ChargingStation>()
                            .eq(ChargingStation::getStationId, station.getStationId())
            );

            if (existingStation != null) {
                // 更新现有充电站
                station.setId(existingStation.getId());
                station.setCreateTime(existingStation.getCreateTime());
                stationMapper.updateById(station);
            } else {
                // 新增充电站
                stationMapper.insert(station);
            }

            // 2. 同步充电桩信息
            if (!CollectionUtils.isEmpty(stationInfo.getEquipmentInfos())) {
                for (EquipmentInfo equipmentInfo : stationInfo.getEquipmentInfos()) {
                    syncEquipmentConnectors(stationInfo.getStationID(), equipmentInfo);
                }
            }

        } catch (Exception e) {
            log.error("同步充电站{}失败: {}", stationInfo.getStationID(), e.getMessage());
        }
    }

    /**
     * 同步设备下的充电桩
     */
    private void syncEquipmentConnectors(String stationId, EquipmentInfo equipmentInfo) {
        if (CollectionUtils.isEmpty(equipmentInfo.getConnectorInfos())) {
            return;
        }

        for (ConnectorInfo connectorInfo : equipmentInfo.getConnectorInfos()) {
            ChargingConnector connector = convertToChargingConnector(stationId, equipmentInfo, connectorInfo);

            // 查询是否已存在
            ChargingConnector existingConnector = connectorMapper.selectOne(
                    new LambdaQueryWrapper<ChargingConnector>()
                            .eq(ChargingConnector::getConnectorId, connector.getConnectorId())
            );

            if (existingConnector != null) {
                // 更新现有充电桩
                connector.setId(existingConnector.getId());
                connector.setCreateTime(existingConnector.getCreateTime());
                connectorMapper.updateById(connector);
            } else {
                // 新增充电桩
                connectorMapper.insert(connector);
            }
        }
    }

    /**
     * 更新充电桩状态
     */
    private void updateConnectorStatus(List<ConnectorStatusInfo> statusInfos) {
        if (CollectionUtils.isEmpty(statusInfos)) {
            return;
        }

        for (ConnectorStatusInfo statusInfo : statusInfos) {
            connectorMapper.update(null,
                    new LambdaUpdateWrapper<ChargingConnector>()
                            .eq(ChargingConnector::getConnectorId, statusInfo.getConnectorID())
                            .set(ChargingConnector::getStatus, statusInfo.getStatus())
                            .set(ChargingConnector::getStatusUpdateTime, LocalDateTime.now())
            );
        }
    }

    /**
     * 转换为充电站实体
     */
    private ChargingStation convertToChargingStation(StationInfo stationInfo) {
        ChargingStation station = new ChargingStation();
        station.setStationId(stationInfo.getStationID());
        station.setStationName(stationInfo.getStationName());
        station.setAddress(stationInfo.getAddress());

        // 解析区域编码
        if (stationInfo.getAreaCode() != null && stationInfo.getAreaCode().length() >= 6) {
            String areaCode = stationInfo.getAreaCode();
            // 简化处理，实际应该有完整的区域码映射
            station.setProvince(areaCode.substring(0, 2) + "0000");
            station.setCity(areaCode.substring(0, 4) + "00");
            station.setDistrict(areaCode);
        }

        station.setLongitude(stationInfo.getStationLng());
        station.setLatitude(stationInfo.getStationLat());
        station.setStationTel(stationInfo.getStationTel());
        station.setParkingFee(stationInfo.getParkFee());
        station.setOpeningHours(stationInfo.getBusineHours());
        station.setStationStatus(stationInfo.getStationStatus());

        return station;
    }

    /**
     * 转换为充电桩实体
     */
    private ChargingConnector convertToChargingConnector(String stationId, EquipmentInfo equipmentInfo, ConnectorInfo connectorInfo) {
        ChargingConnector connector = new ChargingConnector();
        connector.setConnectorId(connectorInfo.getConnectorID());
        connector.setStationId(stationId);
        connector.setConnectorName(connectorInfo.getConnectorName());
        connector.setConnectorType(connectorInfo.getConnectorType());
        connector.setRatedPower(connectorInfo.getPower());
        connector.setCurrentPower(BigDecimal.ZERO);
        connector.setStatus(1); // 默认空闲状态

        return connector;
    }
}
