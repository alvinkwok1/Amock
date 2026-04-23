package com.amock.dto;

import com.amock.entity.MockRule.PathMatchType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MockRuleCreateRequest {
    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String path;

    @NotNull
    private PathMatchType pathMatchType;

    @NotBlank
    private String method;

    private Integer statusCode = 200;

    private String headers;

    private String body;

    private Integer delayMs = 0;

    private String conditions;

    private Integer priority = 0;

    private Boolean enabled = true;

    private String groupId;
}