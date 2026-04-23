package com.amock.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RuleGroupCreateRequest {
    @NotBlank
    private String name;

    private String description;
}