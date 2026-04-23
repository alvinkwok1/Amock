package com.amock.dto;

import com.amock.entity.MockRule.PathMatchType;
import lombok.Data;

@Data
public class MockRuleUpdateRequest {
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
}