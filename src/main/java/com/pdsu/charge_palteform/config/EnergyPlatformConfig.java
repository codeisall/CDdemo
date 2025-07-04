package com.pdsu.charge_palteform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "energy.platform")
public class EnergyPlatformConfig {
    /**
     * 电能平台基础URL
     */
    private String baseUrl;

    /**
     * 运营商ID
     */
    private String operatorId;

    /**
     * 运营商秘钥
     */
    private String operatorSecret ;

    /**
     * 数据加密秘钥
     */
    private String dataSecret;

    /**
     * 数据加密初始化向量
     */
    private String dataSecretIv;

    /**
     * 签名秘钥
     */
    private String sigSecret;

    /**
     * Token有效期（秒）
     */
    private Long tokenExpiration = 7200L;
}
