package com.amock.dto;

import com.amock.entity.RequestLog;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequestLogResponse {
    private String id;
    private String mockRuleId;
    private String method;
    private String path;
    private String queryString;
    private String headers;
    private String body;
    private String clientIp;
    private Integer responseStatus;
    private String responseHeaders;
    private String responseBody;
    private Integer duration;
    private LocalDateTime createdAt;

    public static RequestLogResponse fromEntity(RequestLog entity) {
        RequestLogResponse response = new RequestLogResponse();
        response.setId(entity.getId());
        response.setMockRuleId(entity.getMockRuleId());
        response.setMethod(entity.getMethod());
        response.setPath(entity.getPath());
        response.setQueryString(entity.getQueryString());
        response.setHeaders(entity.getHeaders());
        response.setBody(entity.getBody());
        response.setClientIp(entity.getClientIp());
        response.setResponseStatus(entity.getResponseStatus());
        response.setResponseHeaders(entity.getResponseHeaders());
        response.setResponseBody(entity.getResponseBody());
        response.setDuration(entity.getDuration());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }
}