package com.amock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfigResponse {
    private int retentionDays;
    private double sampleRate;
    private int cacheMaxSize;
}