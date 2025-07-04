package com.pdsu.charge_palteform.config;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // 创建OkHttpClient
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new LoggingInterceptor()) // 添加请求日志
                .build();

        // 创建RestTemplate
        OkHttp3ClientHttpRequestFactory factory = new OkHttp3ClientHttpRequestFactory(okHttpClient);
        return new RestTemplate(factory);
    }

    /**
     * HTTP请求日志拦截器
     */
    private static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            long startTime = System.nanoTime();
            log.debug("发送HTTP请求: {} {}", request.method(), request.url());
            Response response = chain.proceed(request);
            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1e6d; // 转换为毫秒
            log.debug("收到HTTP响应: {} {} (耗时: {:.2f}ms)",
                    response.code(),
                    response.message(),
                    duration);
            return response;
        }
    }
}
