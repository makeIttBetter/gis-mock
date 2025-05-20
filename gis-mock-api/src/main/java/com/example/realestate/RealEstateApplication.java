package com.example.realestate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableFeignClients
@SpringBootApplication
@EnableJpaAuditing
public class RealEstateApplication {
    public static void main(String[] args) {
//        PreInitializationConfig.loadEnvVariables();
        SpringApplication.run(RealEstateApplication.class, args);
    }
}
