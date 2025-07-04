package com.pdsu.charge_palteform.service;

import com.pdsu.charge_palteform.entity.platefrom.station.StationInfo;
import com.pdsu.charge_palteform.entity.platefrom.station.StationStatusInfo;

import java.util.List;

public interface EnergyPlatformService {
    /**
     * 获取电能平台访问Token
     */
    String getAccessToken();

    /**
     * 查询充电站信息
     */
    List<StationInfo> queryStationsInfo(String lastQueryTime, Integer pageNo, Integer pageSize);

    /**
     * 查询充电站状态
     */
    List<StationStatusInfo> queryStationStatus(List<String> stationIds);

    /**
     * 验证Token是否有效
     */
    boolean validateToken(String token);
}
