package com.pdsu.charge_palteform.service;

public interface DataSyncService {
    /**
     * 同步充电站基础信息
     */
    void syncStationInfo();

    /**
     * 同步充电桩状态信息
     */
    void syncConnectorStatus();

    /**
     * 全量同步
     */
    void fullSync();
}
