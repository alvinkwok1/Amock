package com.amock.dto;

import com.amock.entity.RuleGroup;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RuleGroupResponse {
    private String id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RuleGroupResponse fromEntity(RuleGroup entity) {
        RuleGroupResponse response = new RuleGroupResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}