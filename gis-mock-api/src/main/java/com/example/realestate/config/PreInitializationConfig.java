package com.example.realestate.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PreInitializationConfig {

    public static void loadEnvVariables() {
        log.info("Loading environment variables from .env file");
        try {
            loadEnvVariablesFromDotenv();
            log.info("Environment variables loaded successfully.");
        } catch (DotenvException e) {
            log.warn("Error loading environment variables from .env file. " +
                    "You need to set the environment variables manually.");
        }
    }

    private static void loadEnvVariablesFromDotenv() throws DotenvException {
        Dotenv dotenv = Dotenv.load();

        dotenv.entries().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();
            System.setProperty(key, value);
        });
    }
}
