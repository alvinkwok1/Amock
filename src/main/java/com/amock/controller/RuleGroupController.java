package com.amock.controller;

import com.amock.dto.RuleGroupCreateRequest;
import com.amock.dto.RuleGroupResponse;
import com.amock.dto.RuleGroupUpdateRequest;
import com.amock.service.RuleGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class RuleGroupController {

    private final RuleGroupService ruleGroupService;

    @GetMapping
    public List<RuleGroupResponse> getGroups() {
        return ruleGroupService.getGroups();
    }

    @GetMapping("/{id}")
    public RuleGroupResponse getGroup(@PathVariable String id) {
        return ruleGroupService.getGroup(id);
    }

    @PostMapping
    public ResponseEntity<RuleGroupResponse> createGroup(@Valid @RequestBody RuleGroupCreateRequest request) {
        RuleGroupResponse response = ruleGroupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public RuleGroupResponse updateGroup(@PathVariable String id, @RequestBody RuleGroupUpdateRequest request) {
        return ruleGroupService.updateGroup(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id) {
        ruleGroupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
}