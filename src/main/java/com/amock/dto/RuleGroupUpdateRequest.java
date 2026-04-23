package com.amock.dto;

import lombok.Data;

@Data
public class RuleGroupUpdateRequest {
    private String name;

    private String description;
}