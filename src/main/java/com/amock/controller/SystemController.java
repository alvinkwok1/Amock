package com.amock.controller;

import com.amock.dto.ConfigResponse;
import com.amock.dto.HealthResponse;
import com.amock.repository.MockRuleRepository;
import com.amock.service.RuleCacheService;
import com.amock.config.AmockConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SystemController {

    private final MockRuleRepository mockRuleRepository;
    private final RuleCacheService ruleCacheService;
    private final AmockConfig amockConfig;

    @GetMapping("/health")
    public HealthResponse health() {
        long totalRules = mockRuleRepository.count();
        long enabledRules = mockRuleRepository.findByEnabled(true, org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
        String cacheStatus = ruleCacheService.isCacheHealthy() ? "healthy" : "unhealthy";
        return new HealthResponse("UP", totalRules, enabledRules, cacheStatus);
    }

    @GetMapping("/api/config")
    public ConfigResponse getConfig() {
        return new ConfigResponse(
            amockConfig.getRequestLog().getRetentionDays(),
            amockConfig.getRequestLog().getSampleRate(),
            amockConfig.getRule().getCacheMaxSize()
        );
    }
}