package com.pdsu.charge_palteform.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdsu.charge_palteform.config.EnergyPlatformConfig;
import com.pdsu.charge_palteform.entity.platefrom.PlatformRequest;
import com.pdsu.charge_palteform.entity.platefrom.PlatformResponse;
import com.pdsu.charge_palteform.entity.platefrom.station.*;
import com.pdsu.charge_palteform.entity.platefrom.token.TokenRequest;
import com.pdsu.charge_palteform.entity.platefrom.token.TokenResponse;
import com.pdsu.charge_palteform.exception.BusinessException;
import com.pdsu.charge_palteform.service.EnergyPlatformService;
import com.pdsu.charge_palteform.utils.AesUtil;
import com.pdsu.charge_palteform.utils.HMacMD5;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnergyPlatformServiceImpl implements EnergyPlatformService {

    private final EnergyPlatformConfig config;
    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AtomicInteger seqCounter = new AtomicInteger(1);

    private static final String TOKEN_CACHE_KEY = "energy:platform:token";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public String getAccessToken() {
        // 从缓存获取
        String cachedToken = redisTemplate.opsForValue().get(TOKEN_CACHE_KEY);
        if (cachedToken != null) {
            log.debug("使用缓存的Token: {}...", cachedToken.substring(0, Math.min(10, cachedToken.length())));
            return cachedToken;
        }
        // 缓存没有，请求新token
        return requestNewToken();
    }

    /**
     * 请求新的Token
     */
    private String requestNewToken() {
        try {
            log.info("正在向电能平台请求新的访问Token...");

            // 1. 构建请求数据
            TokenRequest tokenRequest = new TokenRequest();
            tokenRequest.setOperatorID(config.getOperatorId());
            tokenRequest.setOperatorSecret(config.getOperatorSecret());

            log.debug("Token请求参数: OperatorID={}, OperatorSecret={}***",
                    config.getOperatorId(),
                    config.getOperatorSecret().substring(0, Math.min(4, config.getOperatorSecret().length())));

            // 2. 加密数据
            String dataJson = objectMapper.writeValueAsString(tokenRequest);
            log.debug("加密前的数据: {}", dataJson);

            String encryptedData = AesUtil.encrypt(dataJson, config.getDataSecret(), config.getDataSecretIv());
            log.debug("加密后的数据: {}", encryptedData);

            // 3. 构建平台请求
            PlatformRequest request = buildPlatformRequest(encryptedData);

            // 4. 打印完整的请求信息
            log.info("发送请求到电能平台:");
            log.info("  URL: {}", config.getBaseUrl() + "/query_token");
            log.info("  OperatorID: {}", request.getOperatorID());
            log.info("  TimeStamp: {}", request.getTimeStamp());
            log.info("  Seq: {}", request.getSeq());
            log.info("  Sig: {}", request.getSig());
            log.info("  Data: {}...", request.getData().substring(0, Math.min(50, request.getData().length())));

            // 5. 发送请求
            String url = config.getBaseUrl() + "/query_token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("User-Agent", "ChargingPlatform/1.0");

            HttpEntity<PlatformRequest> entity = new HttpEntity<>(request, headers);

            // 使用String接收原始响应，便于调试
            ResponseEntity<String> rawResponse = restTemplate.postForEntity(url, entity, String.class);

            log.info("收到原始响应:");
            log.info("  Status: {}", rawResponse.getStatusCode());
            log.info("  Headers: {}", rawResponse.getHeaders());
            log.info("  Body: {}", rawResponse.getBody());

            if (rawResponse.getBody() == null || rawResponse.getBody().trim().isEmpty()) {
                throw new BusinessException("电能平台返回空响应");
            }

            // 尝试解析JSON响应
            PlatformResponse response;
            try {
                response = objectMapper.readValue(rawResponse.getBody(), PlatformResponse.class);
                log.debug("解析后的响应: {}", response);
            } catch (Exception e) {
                log.error("JSON解析失败，原始响应: {}", rawResponse.getBody());
                throw new BusinessException("响应格式错误: " + e.getMessage());
            }

            if (response.getRet() == null) {
                throw new BusinessException("响应中缺少Ret字段，可能是接口地址错误或服务异常");
            }

            if (response.getRet() != 0) {
                throw new BusinessException("电能平台返回错误: Ret=" + response.getRet() + ", Msg=" + response.getMsg());
            }

            if (response.getData() == null || response.getData().trim().isEmpty()) {
                throw new BusinessException("响应数据为空");
            }

            // 6. 解密响应数据
            String decryptedData;
            try {
                decryptedData = AesUtil.decrypt(response.getData(), config.getDataSecret(), config.getDataSecretIv());
                log.debug("解密后的数据: {}", decryptedData);
            } catch (Exception e) {
                log.error("数据解密失败: {}", e.getMessage());
                throw new BusinessException("响应数据解密失败: " + e.getMessage());
            }

            TokenResponse tokenResponse = objectMapper.readValue(decryptedData, TokenResponse.class);
            log.debug("Token响应: {}", tokenResponse);

            if (tokenResponse.getSuccStat() != 0) {
                String errorMsg = getTokenErrorMessage(tokenResponse.getFailReason());
                throw new BusinessException("Token获取失败: " + errorMsg);
            }

            // 7. 缓存Token
            String token = tokenResponse.getAccessToken();
            if (token == null || token.trim().isEmpty()) {
                throw new BusinessException("返回的Token为空");
            }

            long cacheTime = Math.max(tokenResponse.getTokenAvailableTime() - 300, 60);
            redisTemplate.opsForValue().set(TOKEN_CACHE_KEY, token, cacheTime, TimeUnit.SECONDS);

            log.info("✅ 电能平台Token获取成功，有效期: {}秒", tokenResponse.getTokenAvailableTime());
            return token;

        } catch (Exception e) {
            log.error("❌ 获取电能平台Token失败", e);
            throw new BusinessException("获取电能平台Token失败: " + e.getMessage());
        }
    }

    /**
     * 获取Token错误信息
     */
    private String getTokenErrorMessage(Integer failReason) {
        if (failReason == null) {
            return "未知错误";
        }

        switch (failReason) {
            case 0:
                return "无错误";
            case 1:
                return "无此运营商";
            case 2:
                return "密钥错误";
            default:
                return "自定义错误码: " + failReason;
        }
    }

    @Override
    public List<StationInfo> queryStationsInfo(String lastQueryTime, Integer pageNo, Integer pageSize) {
        try {
            log.info("查询电能平台充电站信息，页码: {}, 页大小: {}", pageNo, pageSize);
            // 1. 构建查询请求
            StationQueryPlatformRequest queryRequest = new StationQueryPlatformRequest();
            queryRequest.setLastQueryTime(lastQueryTime);
            queryRequest.setPageNo(pageNo);
            queryRequest.setPageSize(pageSize);

            // 2. 加密数据
            String dataJson = objectMapper.writeValueAsString(queryRequest);
            log.debug("充电站查询请求数据: {}", dataJson);
            String encryptedData = AesUtil.encrypt(dataJson, config.getDataSecret(), config.getDataSecretIv());

            // 3. 构建平台请求
            PlatformRequest request = buildPlatformRequest(encryptedData);

            // 4. 发送请求
            String url = config.getBaseUrl() + "/query_stations_info";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(getAccessToken());

            HttpEntity<PlatformRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<PlatformResponse> response = restTemplate.postForEntity(url, entity, PlatformResponse.class);

            if (response.getBody() == null || response.getBody().getRet() != 0) {
                throw new BusinessException("查询充电站信息失败");
            }

            // 5. 解密响应数据
            String decryptedData = AesUtil.decrypt(response.getBody().getData(),
                    config.getDataSecret(), config.getDataSecretIv());

            // 6. 解析充电站列表
            StationQueryResponse queryResponse = objectMapper.readValue(decryptedData, StationQueryResponse.class);

            log.info("查询到{}个充电站", queryResponse.getStationInfos().size());
            return queryResponse.getStationInfos();

        } catch (Exception e) {
            log.error("查询电能平台充电站信息失败", e);
            throw new BusinessException("查询充电站信息失败: " + e.getMessage());
        }
    }

    public boolean  invalidateToken(String token) {
        try {
            return token != null && !token.isEmpty();
        } catch (Exception e) {
            log.error("验证Token失败", e);
            return false;
        }
    }



    @Override
    public List<StationStatusInfo> queryStationStatus(List<String> stationIds) {
        try {
            log.info("查询{}个充电站状态", stationIds.size());

            // 1. 构建状态查询请求
            StationStatusRequest statusRequest = new StationStatusRequest();
            statusRequest.setStationIDs(stationIds);

            // 2. 加密数据
            String dataJson = objectMapper.writeValueAsString(statusRequest);
            String encryptedData = AesUtil.encrypt(dataJson, config.getDataSecret(), config.getDataSecretIv());

            // 3. 构建平台请求
            PlatformRequest request = buildPlatformRequest(encryptedData);

            // 4. 发送请求
            String url = config.getBaseUrl() + "/query_station_status";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(getAccessToken());

            HttpEntity<PlatformRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<PlatformResponse> response = restTemplate.postForEntity(url, entity, PlatformResponse.class);

            if (response.getBody() == null || response.getBody().getRet() != 0) {
                throw new BusinessException("查询充电站状态失败");
            }

            // 5. 解密响应数据
            String decryptedData = AesUtil.decrypt(response.getBody().getData(),
                    config.getDataSecret(), config.getDataSecretIv());

            StationStatusResponse statusResponse = objectMapper.readValue(decryptedData, StationStatusResponse.class);

            return statusResponse.getStationStatusInfos();

        } catch (Exception e) {
            log.error("查询充电站状态失败", e);
            throw new BusinessException("查询充电站状态失败: " + e.getMessage());
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            // 这里可以调用电能平台的Token验证接口，暂时简化处理
            return token != null && !token.isEmpty();
        } catch (Exception e) {
            log.error("验证Token失败", e);
            return false;
        }
    }

    /**
     * 构建电能平台通用请求
     */
    private PlatformRequest buildPlatformRequest(String encryptedData) {
        PlatformRequest request = new PlatformRequest();
        request.setOperatorID(config.getOperatorId());
        request.setData(encryptedData);

        // 生成时间戳
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        request.setTimeStamp(timestamp);

        // 生成序列号
        String seq = String.format("%04d", seqCounter.getAndIncrement() % 10000);
        request.setSeq(seq);

        // 生成签名
        String signContent = config.getOperatorId() + encryptedData + timestamp + seq;
        String signature = HMacMD5.getHmacMd5Str(config.getSigSecret(), signContent);
        request.setSig(signature);

        log.debug("构建请求签名:");
        log.debug("  签名内容: {}", signContent);
        log.debug("  签名密钥: {}***", config.getSigSecret().substring(0, Math.min(4, config.getSigSecret().length())));
        log.debug("  最终签名: {}", signature);

        return request;
    }
}
