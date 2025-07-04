package com.pdsu.charge_palteform.config;


import com.pdsu.charge_palteform.service.DataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.schedule.enabled", havingValue = "true", matchIfMissing = true)
public class ScheduleConfig {

    private final DataSyncService dataSyncService;

    /**
     * 每天凌晨2点同步充电站基础信息
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncStationInfo() {
        log.info("开始定时同步充电站基础信息...");
        try {
            dataSyncService.syncStationInfo();
            log.info("定时同步充电站基础信息完成");
        } catch (Exception e) {
            log.error("定时同步充电站基础信息失败", e);
        }
    }

    /**
     * 每5分钟同步充电桩状态
     */
    @Scheduled(fixedRate = 300000) // 5分钟 = 300000毫秒
    public void syncConnectorStatus() {
        log.debug("开始定时同步充电桩状态...");
        try {
            dataSyncService.syncConnectorStatus();
            log.debug("定时同步充电桩状态完成");
        } catch (Exception e) {
            log.error("定时同步充电桩状态失败", e);
        }
    }
}
