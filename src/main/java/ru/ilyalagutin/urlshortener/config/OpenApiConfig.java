package ru.ilyalagutin.urlshortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI shortenerOpenApi() {
        return new OpenAPI().info(new Info()
                .title("URL Shortener Service API")
                .version("v1")
                .description("API for creating and managing short URLs"));
    }
}