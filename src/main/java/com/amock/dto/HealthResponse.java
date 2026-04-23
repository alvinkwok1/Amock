package com.amock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HealthResponse {
    private String status;
    private long ruleCount;
    private long enabledRuleCount;
    private String cacheStatus;
}