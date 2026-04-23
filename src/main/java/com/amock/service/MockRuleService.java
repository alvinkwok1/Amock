package com.amock.service;

import com.amock.dto.MockRuleCreateRequest;
import com.amock.dto.MockRuleResponse;
import com.amock.dto.MockRuleUpdateRequest;
import com.amock.dto.PageResponse;
import com.amock.entity.MockRule;
import com.amock.exception.ResourceNotFoundException;
import com.amock.repository.MockRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MockRuleService {

    private final MockRuleRepository mockRuleRepository;
    private final RuleCacheService ruleCacheService;

    public PageResponse<MockRuleResponse> getRules(Boolean enabled, String method, String groupId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "priority"));
        Page<MockRule> rules = mockRuleRepository.findByFilters(enabled, method, groupId, pageRequest);
        Page<MockRuleResponse> responses = rules.map(MockRuleResponse::fromEntity);
        return PageResponse.fromPage(responses);
    }

    public MockRuleResponse getRule(String id) {
        MockRule rule = mockRuleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("MockRule", id));
        return MockRuleResponse.fromEntity(rule);
    }

    @Transactional
    public MockRuleResponse createRule(MockRuleCreateRequest request) {
        MockRule rule = MockRule.builder()
            .name(request.getName())
            .description(request.getDescription())
            .path(request.getPath())
            .pathMatchType(request.getPathMatchType())
            .method(request.getMethod())
            .statusCode(request.getStatusCode())
            .headers(request.getHeaders())
            .body(request.getBody())
            .delayMs(request.getDelayMs())
            .conditions(request.getConditions())
            .priority(request.getPriority())
            .enabled(request.getEnabled())
            .groupId(request.getGroupId())
            .build();

        MockRule saved = mockRuleRepository.save(rule);
        ruleCacheService.refreshCache();
        return MockRuleResponse.fromEntity(saved);
    }

    @Transactional
    public MockRuleResponse updateRule(String id, MockRuleUpdateRequest request) {
        MockRule rule = mockRuleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("MockRule", id));

        if (request.getName() != null) rule.setName(request.getName());
        if (request.getDescription() != null) rule.setDescription(request.getDescription());
        if (request.getPath() != null) rule.setPath(request.getPath());
        if (request.getPathMatchType() != null) rule.setPathMatchType(request.getPathMatchType());
        if (request.getMethod() != null) rule.setMethod(request.getMethod());
        if (request.getStatusCode() != null) rule.setStatusCode(request.getStatusCode());
        if (request.getHeaders() != null) rule.setHeaders(request.getHeaders());
        if (request.getBody() != null) rule.setBody(request.getBody());
        if (request.getDelayMs() != null) rule.setDelayMs(request.getDelayMs());
        if (request.getConditions() != null) rule.setConditions(request.getConditions());
        if (request.getPriority() != null) rule.setPriority(request.getPriority());
        if (request.getEnabled() != null) rule.setEnabled(request.getEnabled());
        if (request.getGroupId() != null) rule.setGroupId(request.getGroupId());

        MockRule updated = mockRuleRepository.save(rule);
        ruleCacheService.refreshCache();
        return MockRuleResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteRule(String id) {
        if (!mockRuleRepository.existsById(id)) {
            throw new ResourceNotFoundException("MockRule", id);
        }
        mockRuleRepository.deleteById(id);
        ruleCacheService.refreshCache();
    }

    @Transactional
    public MockRuleResponse copyRule(String id) {
        MockRule original = mockRuleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("MockRule", id));

        MockRule copy = MockRule.builder()
            .name(original.getName() + " (Copy)")
            .description(original.getDescription())
            .path(original.getPath())
            .pathMatchType(original.getPathMatchType())
            .method(original.getMethod())
            .statusCode(original.getStatusCode())
            .headers(original.getHeaders())
            .body(original.getBody())
            .delayMs(original.getDelayMs())
            .conditions(original.getConditions())
            .priority(original.getPriority())
            .enabled(false)
            .groupId(original.getGroupId())
            .build();

        MockRule saved = mockRuleRepository.save(copy);
        ruleCacheService.refreshCache();
        return MockRuleResponse.fromEntity(saved);
    }

    @Transactional
    public List<MockRuleResponse> importRules(List<MockRuleCreateRequest> requests) {
        List<MockRule> rules = requests.stream()
            .map(req -> MockRule.builder()
                .name(req.getName())
                .description(req.getDescription())
                .path(req.getPath())
                .pathMatchType(req.getPathMatchType())
                .method(req.getMethod())
                .statusCode(req.getStatusCode())
                .headers(req.getHeaders())
                .body(req.getBody())
                .delayMs(req.getDelayMs())
                .conditions(req.getConditions())
                .priority(req.getPriority())
                .enabled(req.getEnabled())
                .groupId(req.getGroupId())
                .build())
            .collect(Collectors.toList());

        List<MockRule> saved = mockRuleRepository.saveAll(rules);
        ruleCacheService.refreshCache();
        return saved.stream().map(MockRuleResponse::fromEntity).collect(Collectors.toList());
    }

    public List<MockRuleResponse> exportRules(Boolean enabled, String groupId) {
        List<MockRule> rules;
        if (enabled != null && groupId != null) {
            rules = mockRuleRepository.findByFilters(enabled, null, groupId, PageRequest.of(0, 1000)).getContent();
        } else if (enabled != null) {
            rules = mockRuleRepository.findByEnabled(enabled, PageRequest.of(0, 1000)).getContent();
        } else if (groupId != null) {
            rules = mockRuleRepository.findByGroupId(groupId, PageRequest.of(0, 1000)).getContent();
        } else {
            rules = mockRuleRepository.findAll();
        }
        return rules.stream().map(MockRuleResponse::fromEntity).collect(Collectors.toList());
    }
}