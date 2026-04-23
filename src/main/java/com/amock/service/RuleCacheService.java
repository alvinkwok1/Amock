package com.amock.service;

import com.amock.entity.MockRule;
import com.amock.repository.MockRuleRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleCacheService {

    private final MockRuleRepository mockRuleRepository;

    @Getter
    private volatile List<MockRule> cachedRules = List.of();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private Cache<String, MockRule> ruleCache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build();

    public List<MockRule> getEnabledRules() {
        lock.readLock().lock();
        try {
            return cachedRules;
        } finally {
            lock.readLock().unlock();
        }
    }

    public MockRule getRuleById(String id) {
        return ruleCache.get(id, key -> mockRuleRepository.findById(key).orElse(null));
    }

    public void refreshCache() {
        lock.writeLock().lock();
        try {
            cachedRules = mockRuleRepository.findByEnabledTrueOrderByPriorityDesc();
            ruleCache.invalidateAll();
            log.info("Rule cache refreshed, {} active rules loaded", cachedRules.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void init() {
        refreshCache();
    }

    public boolean isCacheHealthy() {
        return cachedRules != null;
    }
}