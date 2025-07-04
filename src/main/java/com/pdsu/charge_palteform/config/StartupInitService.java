package com.pdsu.charge_palteform.config;


import com.pdsu.charge_palteform.service.EnergyPlatformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Order(1) // 优先级最高，第一个执行
@RequiredArgsConstructor
public class StartupInitService implements ApplicationRunner {

    private final EnergyPlatformService energyPlatformService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("================ 应用启动初始化开始 ================");

        // 1. 测试电能平台连接
        testEnergyPlatformConnection();

        // 2. 预热Token缓存
        warmupTokenCache();

        log.info("================ 应用启动初始化完成 ================");
    }

    /**
     * 测试电能平台连接
     */
    private void testEnergyPlatformConnection() {
        try {
            log.info("正在测试电能平台连接...");

            // 尝试获取Token
            String token = energyPlatformService.getAccessToken();

            if (token != null && !token.isEmpty()) {
                log.info("✅ 电能平台连接成功！Token前缀: {}...",
                        token.substring(0, Math.min(10, token.length())));
            } else {
                log.error("❌ 电能平台连接失败：Token为空");
            }

        } catch (Exception e) {
            log.error("❌ 电能平台连接失败: {}", e.getMessage());
            log.warn("⚠️  系统将以离线模式启动，部分功能可能受限");
        }
    }

    /**
     * 预热Token缓存
     */
    private void warmupTokenCache() {
        try {
            log.info("正在预热Token缓存...");

            // 验证Token是否有效
            String token = energyPlatformService.getAccessToken();
            boolean isValid = energyPlatformService.validateToken(token);

            if (isValid) {
                log.info("✅ Token缓存预热成功");
            } else {
                log.warn("⚠️  Token验证失败，可能需要重新获取");
            }

        } catch (Exception e) {
            log.error("❌ Token缓存预热失败: {}", e.getMessage());
        }
    }
}
