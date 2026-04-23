package com.amock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class RequestStatsResponse {
    private long totalRequests;
    private Map<String, Long> requestsByMethod;
    private Double averageDuration;
    private long requestsToday;
}