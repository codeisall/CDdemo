package com.pdsu.charge_palteform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdsu.charge_palteform.entity.ChargingConnector;
import com.pdsu.charge_palteform.entity.ChargingStation;
import com.pdsu.charge_palteform.entity.dto.PageResponse;
import com.pdsu.charge_palteform.entity.dto.StationDetailResponse;
import com.pdsu.charge_palteform.entity.dto.StationListResponse;
import com.pdsu.charge_palteform.entity.dto.StationQueryRequest;
import com.pdsu.charge_palteform.enums.ConnectorStatusEnum;
import com.pdsu.charge_palteform.enums.ConnectorTypeEnum;
import com.pdsu.charge_palteform.enums.StationStatusEnum;
import com.pdsu.charge_palteform.exception.BusinessException;
import com.pdsu.charge_palteform.mapper.ChargingConnectorMapper;
import com.pdsu.charge_palteform.mapper.ChargingStationMapper;
import com.pdsu.charge_palteform.service.ChargingStationService;
import com.pdsu.charge_palteform.service.DataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChargingStationServiceImpl extends ServiceImpl<ChargingStationMapper, ChargingStation> implements ChargingStationService {

    private final ChargingStationMapper stationMapper;
    private final ChargingConnectorMapper connectorMapper;
    private final DataSyncService dataSyncService;

    @Override
    public PageResponse<StationListResponse> queryNearbyStations(StationQueryRequest request) {
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new BusinessException("位置信息不能为空");
        }

        // 计算分页偏移量
        int offset = (request.getPageNum() - 1) * request.getPageSize();

        // 查询附近充电站
        List<ChargingStation> stations = stationMapper.findNearbyStations(
                request.getLatitude(),
                request.getLongitude(),
                request.getRadius(),
                offset,
                request.getPageSize()
        );

        // 统计总数
        Long total = stationMapper.countNearbyStations(
                request.getLatitude(),
                request.getLongitude(),
                request.getRadius()
        );

        // 如果本地没有数据，尝试同步
        if (total == 0) {
            log.info("本地无充电站数据，尝试从电能平台同步...");
            try {
                dataSyncService.syncStationInfo();
                // 重新查询
                stations = stationMapper.findNearbyStations(
                        request.getLatitude(),
                        request.getLongitude(),
                        request.getRadius(),
                        offset,
                        request.getPageSize()
                );
                total = stationMapper.countNearbyStations(
                        request.getLatitude(),
                        request.getLongitude(),
                        request.getRadius()
                );
            } catch (Exception e) {
                log.error("同步充电站数据失败", e);
                // 同步失败不影响查询，返回空结果
            }
        }

        // 转换为响应DTO
        List<StationListResponse> responseList = convertToStationList(stations, request);

        return PageResponse.of(responseList, total, request.getPageNum(), request.getPageSize());
    }

    @Override
    public PageResponse<StationListResponse> searchStations(StationQueryRequest request) {
        LambdaQueryWrapper<ChargingStation> queryWrapper = new LambdaQueryWrapper<ChargingStation>()
                .eq(ChargingStation::getStationStatus, 2); // 只查询运营中的充电站

        // 关键词搜索
        if (StringUtils.hasText(request.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(ChargingStation::getStationName, request.getKeyword())
                    .or()
                    .like(ChargingStation::getAddress, request.getKeyword())
            );
        }

        // 分页查询
        Page<ChargingStation> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<ChargingStation> result = page(page, queryWrapper);

        // 如果搜索结果为空且是第一次查询，尝试同步数据
        if (result.getTotal() == 0 && request.getPageNum() == 1) {
            log.info("搜索无结果，尝试同步最新数据...");
            try {
                dataSyncService.syncStationInfo();
                // 重新搜索
                result = page(page, queryWrapper);
            } catch (Exception e) {
                log.error("同步数据失败", e);
            }
        }

        // 转换为响应DTO
        List<StationListResponse> responseList = convertToStationList(result.getRecords(), request);

        return PageResponse.of(responseList, result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public StationDetailResponse getStationDetail(String stationId, BigDecimal latitude, BigDecimal longitude) {
        // 查询充电站信息
        ChargingStation station = getOne(new LambdaQueryWrapper<ChargingStation>()
                .eq(ChargingStation::getStationId, stationId));

        if (station == null) {
            throw new BusinessException("充电站不存在");
        }

        // 查询充电桩信息
        List<ChargingConnector> connectors = connectorMapper.selectList(
                new LambdaQueryWrapper<ChargingConnector>()
                        .eq(ChargingConnector::getStationId, stationId)
                        .orderByAsc(ChargingConnector::getConnectorId)
        );

        // 构建响应对象
        StationDetailResponse response = new StationDetailResponse();
        response.setStationId(station.getStationId());
        response.setStationName(station.getStationName());
        response.setAddress(station.getAddress());
        response.setLongitude(station.getLongitude());
        response.setLatitude(station.getLatitude());
        response.setStationTel(station.getStationTel());
        response.setServiceFee(station.getServiceFee());
        response.setParkingFee(station.getParkingFee());
        response.setOpeningHours(station.getOpeningHours());
        response.setStationStatus(station.getStationStatus());
        response.setStatusText(StationStatusEnum.getDesc(station.getStationStatus()));

        // 计算距离
        if (latitude != null && longitude != null &&
                station.getLatitude() != null && station.getLongitude() != null) {
            BigDecimal distance = calculateDistance(latitude, longitude,
                    station.getLatitude(), station.getLongitude());
            response.setDistance(distance);
        }

        // 统计充电桩信息
        response.setTotalConnectors(connectors.size());

        Map<Integer, Long> statusCount = connectors.stream()
                .collect(Collectors.groupingBy(ChargingConnector::getStatus, Collectors.counting()));

        response.setAvailableConnectors(statusCount.getOrDefault(1, 0L).intValue()); // 空闲
        response.setChargingConnectors(statusCount.getOrDefault(2, 0L).intValue()); // 充电中
        response.setFaultConnectors(statusCount.getOrDefault(255, 0L).intValue()); // 故障

        // 转换充电桩详细信息
        List<StationDetailResponse.ConnectorDetail> connectorInfos = connectors.stream()
                .map(this::convertToConnectorInfo)
                .collect(Collectors.toList());
        response.setConnectors(connectorInfos);

        return response;
    }

    @Override
    public List<ChargingConnector> getStationConnectors(String stationId) {
        return connectorMapper.selectList(new LambdaQueryWrapper<ChargingConnector>()
                .eq(ChargingConnector::getStationId, stationId)
                .orderByAsc(ChargingConnector::getConnectorId));
    }

    @Override
    public ChargingConnector getConnectorById(String connectorId) {
        ChargingConnector connector = connectorMapper.selectOne(
                new LambdaQueryWrapper<ChargingConnector>()
                        .eq(ChargingConnector::getConnectorId, connectorId)
        );

        if (connector == null) {
            throw new BusinessException("充电桩不存在");
        }

        return connector;
    }

    /**
     * 转换为充电站列表响应DTO
     */
    private List<StationListResponse> convertToStationList(List<ChargingStation> stations, StationQueryRequest request) {
        if (stations.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询充电桩统计信息
        List<String> stationIds = stations.stream()
                .map(ChargingStation::getStationId)
                .collect(Collectors.toList());

        List<Map<String, Object>> connectorStats = connectorMapper.batchCountConnectorsByStatus(stationIds);

        // 按充电站ID分组
        Map<String, Map<Integer, Integer>> stationConnectorStats = new HashMap<>();
        for (Map<String, Object> stat : connectorStats) {
            String stationId = (String) stat.get("station_id");
            Integer status = (Integer) stat.get("status");
            Integer count = ((Number) stat.get("count")).intValue();

            stationConnectorStats.computeIfAbsent(stationId, k -> new HashMap<>())
                    .put(status, count);
        }

        // 查询价格范围（简化处理，这里可以优化为批量查询）
        return stations.stream().map(station -> {
            StationListResponse response = new StationListResponse();
            response.setStationId(station.getStationId());
            response.setStationName(station.getStationName());
            response.setAddress(station.getAddress());
            response.setLongitude(station.getLongitude());
            response.setLatitude(station.getLatitude());
            response.setStationTel(station.getStationTel());
            response.setServiceFee(station.getServiceFee());
            response.setOpeningHours(station.getOpeningHours());
            response.setStationStatus(station.getStationStatus());
            response.setStatusText(StationStatusEnum.getDesc(station.getStationStatus()));

            // 计算距离
            if (request.getLatitude() != null && request.getLongitude() != null &&
                    station.getLatitude() != null && station.getLongitude() != null) {
                BigDecimal distance = calculateDistance(request.getLatitude(), request.getLongitude(),
                        station.getLatitude(), station.getLongitude());
                response.setDistance(distance);
            }

            // 设置充电桩统计信息
            Map<Integer, Integer> stats = stationConnectorStats.getOrDefault(station.getStationId(), new HashMap<>());
            int total = stats.values().stream().mapToInt(Integer::intValue).sum();
            response.setTotalConnectors(total);
            response.setAvailableConnectors(stats.getOrDefault(1, 0)); // 空闲
            response.setChargingConnectors(stats.getOrDefault(2, 0)); // 充电中
            response.setFaultConnectors(stats.getOrDefault(255, 0)); // 故障

            // 查询价格范围（这里简化处理）
            List<ChargingConnector> connectors = getStationConnectors(station.getStationId());
            if (!connectors.isEmpty()) {
                OptionalDouble minFee = connectors.stream()
                        .filter(c -> c.getElectricityFee() != null)
                        .mapToDouble(c -> c.getElectricityFee().doubleValue())
                        .min();
                OptionalDouble maxFee = connectors.stream()
                        .filter(c -> c.getElectricityFee() != null)
                        .mapToDouble(c -> c.getElectricityFee().doubleValue())
                        .max();

                if (minFee.isPresent()) {
                    response.setMinElectricityFee(BigDecimal.valueOf(minFee.getAsDouble()));
                }
                if (maxFee.isPresent()) {
                    response.setMaxElectricityFee(BigDecimal.valueOf(maxFee.getAsDouble()));
                }
            }

            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 转换为充电桩信息DTO
     */
    private StationDetailResponse.ConnectorDetail convertToConnectorInfo(ChargingConnector connector) {
        StationDetailResponse.ConnectorDetail info = new StationDetailResponse.ConnectorDetail();
        info.setConnectorId(connector.getConnectorId());
        info.setConnectorName(connector.getConnectorName());
        info.setConnectorType(connector.getConnectorType());
        info.setConnectorTypeText(ConnectorTypeEnum.getDesc(connector.getConnectorType()));
        info.setRatedPower(connector.getRatedPower());
        info.setCurrentPower(connector.getCurrentPower());
        info.setElectricityFee(connector.getElectricityFee());
        info.setServiceFee(connector.getServiceFee());
        info.setStatus(connector.getStatus());
        info.setStatusText(ConnectorStatusEnum.getDesc(connector.getStatus()));
        return info;
    }

    /**
     * 计算两点间距离（公里）
     */
    private BigDecimal calculateDistance(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        final double EARTH_RADIUS = 6371; // 地球半径（公里）

        double radLat1 = Math.toRadians(lat1.doubleValue());
        double radLat2 = Math.toRadians(lat2.doubleValue());
        double deltaLat = Math.toRadians(lat2.subtract(lat1).doubleValue());
        double deltaLng = Math.toRadians(lng2.subtract(lng1).doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(radLat1) * Math.cos(radLat2) *
                        Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;

        return BigDecimal.valueOf(distance).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
