package com.amock.dto;

import com.amock.entity.MockRule;
import com.amock.entity.MockRule.PathMatchType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MockRuleResponse {
    private String id;
    private String name;
    private String description;
    private String path;
    private PathMatchType pathMatchType;
    private String method;
    private Integer statusCode;
    private String headers;
    private String body;
    private Integer delayMs;
    private String conditions;
    private Integer priority;
    private Boolean enabled;
    private String groupId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MockRuleResponse fromEntity(MockRule entity) {
        MockRuleResponse response = new MockRuleResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setPath(entity.getPath());
        response.setPathMatchType(entity.getPathMatchType());
        response.setMethod(entity.getMethod());
        response.setStatusCode(entity.getStatusCode());
        response.setHeaders(entity.getHeaders());
        response.setBody(entity.getBody());
        response.setDelayMs(entity.getDelayMs());
        response.setConditions(entity.getConditions());
        response.setPriority(entity.getPriority());
        response.setEnabled(entity.getEnabled());
        response.setGroupId(entity.getGroupId());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}