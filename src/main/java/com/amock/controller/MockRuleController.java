package com.amock.controller;

import com.amock.dto.MockRuleCreateRequest;
import com.amock.dto.MockRuleResponse;
import com.amock.dto.MockRuleUpdateRequest;
import com.amock.dto.PageResponse;
import com.amock.service.MockRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class MockRuleController {

    private final MockRuleService mockRuleService;

    @GetMapping
    public PageResponse<MockRuleResponse> getRules(
        @RequestParam(required = false) Boolean enabled,
        @RequestParam(required = false) String method,
        @RequestParam(required = false) String groupId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size) {
        return mockRuleService.getRules(enabled, method, groupId, page, size);
    }

    @GetMapping("/{id}")
    public MockRuleResponse getRule(@PathVariable String id) {
        return mockRuleService.getRule(id);
    }

    @PostMapping
    public ResponseEntity<MockRuleResponse> createRule(@Valid @RequestBody MockRuleCreateRequest request) {
        MockRuleResponse response = mockRuleService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public MockRuleResponse updateRule(@PathVariable String id, @RequestBody MockRuleUpdateRequest request) {
        return mockRuleService.updateRule(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable String id) {
        mockRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/copy")
    public ResponseEntity<MockRuleResponse> copyRule(@PathVariable String id) {
        MockRuleResponse response = mockRuleService.copyRule(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/import")
    public ResponseEntity<List<MockRuleResponse>> importRules(@Valid @RequestBody List<MockRuleCreateRequest> requests) {
        List<MockRuleResponse> responses = mockRuleService.importRules(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping("/export")
    public List<MockRuleResponse> exportRules(
        @RequestParam(required = false) Boolean enabled,
        @RequestParam(required = false) String groupId) {
        return mockRuleService.exportRules(enabled, groupId);
    }
}