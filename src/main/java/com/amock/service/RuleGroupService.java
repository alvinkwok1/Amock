package com.amock.service;

import com.amock.dto.RuleGroupCreateRequest;
import com.amock.dto.RuleGroupResponse;
import com.amock.dto.RuleGroupUpdateRequest;
import com.amock.entity.RuleGroup;
import com.amock.exception.ResourceNotFoundException;
import com.amock.repository.MockRuleRepository;
import com.amock.repository.RuleGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuleGroupService {

    private final RuleGroupRepository ruleGroupRepository;
    private final MockRuleRepository mockRuleRepository;

    public List<RuleGroupResponse> getGroups() {
        return ruleGroupRepository.findAll()
            .stream()
            .map(RuleGroupResponse::fromEntity)
            .collect(Collectors.toList());
    }

    public RuleGroupResponse getGroup(String id) {
        RuleGroup group = ruleGroupRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RuleGroup", id));
        return RuleGroupResponse.fromEntity(group);
    }

    @Transactional
    public RuleGroupResponse createGroup(RuleGroupCreateRequest request) {
        RuleGroup group = RuleGroup.builder()
            .name(request.getName())
            .description(request.getDescription())
            .build();

        RuleGroup saved = ruleGroupRepository.save(group);
        return RuleGroupResponse.fromEntity(saved);
    }

    @Transactional
    public RuleGroupResponse updateGroup(String id, RuleGroupUpdateRequest request) {
        RuleGroup group = ruleGroupRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RuleGroup", id));

        if (request.getName() != null) group.setName(request.getName());
        if (request.getDescription() != null) group.setDescription(request.getDescription());

        RuleGroup updated = ruleGroupRepository.save(group);
        return RuleGroupResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteGroup(String id) {
        if (!ruleGroupRepository.existsById(id)) {
            throw new ResourceNotFoundException("RuleGroup", id);
        }

        long ruleCount = mockRuleRepository.countByGroupId(id);
        if (ruleCount > 0) {
            throw new IllegalStateException("Cannot delete group with existing rules. Remove or reassign rules first.");
        }

        ruleGroupRepository.deleteById(id);
    }
}