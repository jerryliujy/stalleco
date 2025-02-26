package org.example.stalleco_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "openai")
@Data
public class OpenAIConfig {
    private String apiKey;
    private String baseUrl;
    private String model;
}