package com.amock.config;

import com.amock.repository.RequestLogRepository;
import com.amock.service.RuleCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulingConfig {

    private final RequestLogRepository requestLogRepository;
    private final RuleCacheService ruleCacheService;
    private final AmockConfig amockConfig;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        ruleCacheService.init();
        log.info("Application ready, rule cache initialized");
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupRequestLogs() {
        int retentionDays = amockConfig.getRequestLog().getRetentionDays();
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);

        int deleted = requestLogRepository.deleteByCreatedAtBefore(cutoffTime);
        log.info("Cleaned up {} request logs older than {} days", deleted, retentionDays);
    }

    @Scheduled(fixedRate = 300000)
    public void refreshRuleCache() {
        ruleCacheService.refreshCache();
    }
}