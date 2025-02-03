package com.example.realestate;

import com.example.realestate.config.PreInitializationConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class RealEstateApplication {
    public static void main(String[] args) {
        PreInitializationConfig.loadEnvVariables();
        SpringApplication.run(RealEstateApplication.class, args);
    }
}
