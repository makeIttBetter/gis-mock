// File: src/main/java/com/example/realestate/client/ArcgisClientConfig.java
package com.example.realestate.client;

import feign.Logger;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Slf4j
public class ArcgisClientConfig {

    @Value("${arcgis.api.key}")
    private String arcgisApiKey;

    @Bean
    public RequestInterceptor arcgisRequestInterceptor() {
        return requestTemplate -> {
            String authorizationHeader = "Bearer " + arcgisApiKey;
            requestTemplate.header("Authorization", authorizationHeader);
            log.debug("Set Authorization header: {}", authorizationHeader);
        };
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
