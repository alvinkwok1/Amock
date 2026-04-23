package com.amock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "amock")
public class AmockConfig {

    private RequestLogConfig requestLog = new RequestLogConfig();
    private RuleConfig rule = new RuleConfig();

    @Data
    public static class RequestLogConfig {
        private int retentionDays = 7;
        private double sampleRate = 1.0;
    }

    @Data
    public static class RuleConfig {
        private int cacheMaxSize = 1000;
    }
}