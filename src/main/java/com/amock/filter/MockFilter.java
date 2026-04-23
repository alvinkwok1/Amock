package com.amock.filter;

import com.amock.config.AmockConfig;
import com.amock.entity.MockRule;
import com.amock.entity.RequestLog;
import com.amock.repository.RequestLogRepository;
import com.amock.service.RuleCacheService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class MockFilter implements Filter {

    private final RuleCacheService ruleCacheService;
    private final RequestLogRepository requestLogRepository;
    private final ConditionMatcher conditionMatcher;
    private final AmockConfig amockConfig;
    private final ObjectMapper objectMapper;

    private final Random random = new Random();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        if (!path.startsWith("/mock/")) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);

        long startTime = System.currentTimeMillis();

        MockRule matchedRule = findMatchingRule(requestWrapper);

        if (matchedRule != null) {
            handleMockResponse(requestWrapper, responseWrapper, matchedRule, startTime);
            responseWrapper.copyBodyToResponse();
            return;
        }

        chain.doFilter(requestWrapper, responseWrapper);
        responseWrapper.copyBodyToResponse();

        if (shouldSample()) {
            saveRequestLog(requestWrapper, responseWrapper, null, startTime);
        }
    }

    private MockRule findMatchingRule(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        for (MockRule rule : ruleCacheService.getEnabledRules()) {
            if (matchesPath(rule, path) && matchesMethod(rule, method)) {
                if (rule.getConditions() != null && !rule.getConditions().isEmpty()) {
                    if (!conditionMatcher.matches(rule.getConditions(), request)) {
                        continue;
                    }
                }
                return rule;
            }
        }
        return null;
    }

    private boolean matchesPath(MockRule rule, String requestPath) {
        String rulePath = rule.getPath();

        switch (rule.getPathMatchType()) {
            case EXACT:
                return requestPath.equals(rulePath);
            case PREFIX:
                return requestPath.startsWith(rulePath);
            case REGEX:
                return requestPath.matches(rulePath);
            default:
                return false;
        }
    }

    private boolean matchesMethod(MockRule rule, String method) {
        if ("ANY".equalsIgnoreCase(rule.getMethod())) {
            return true;
        }
        return rule.getMethod().equalsIgnoreCase(method);
    }

    private void handleMockResponse(HttpServletRequest request, HttpServletResponse response,
                                    MockRule rule, long startTime) throws IOException {

        if (rule.getDelayMs() > 0) {
            try {
                Thread.sleep(rule.getDelayMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        response.setStatus(rule.getStatusCode());

        if (rule.getHeaders() != null && !rule.getHeaders().isEmpty()) {
            try {
                JsonNode headersNode = objectMapper.readTree(rule.getHeaders());
                headersNode.fields().forEachRemaining(entry ->
                    response.setHeader(entry.getKey(), entry.getValue().asText())
                );
            } catch (Exception e) {
                log.warn("Failed to parse headers JSON: {}", e.getMessage());
            }
        }

        if (rule.getBody() != null) {
            response.getWriter().write(rule.getBody());
        }

        long duration = System.currentTimeMillis() - startTime;

        if (shouldSample()) {
            saveRequestLog(request, response, rule, startTime);
        }
    }

    private boolean shouldSample() {
        double sampleRate = amockConfig.getRequestLog().getSampleRate();
        return random.nextDouble() < sampleRate;
    }

    private void saveRequestLog(HttpServletRequest request, HttpServletResponse response,
                                MockRule rule, long startTime) {

        try {
            RequestLog log = RequestLog.builder()
                .mockRuleId(rule != null ? rule.getId() : null)
                .method(request.getMethod())
                .path(request.getRequestURI())
                .queryString(request.getQueryString())
                .headers(extractHeaders(request))
                .body(extractBody(request))
                .clientIp(request.getRemoteAddr())
                .responseStatus(response.getStatus())
                .responseHeaders(extractResponseHeaders(response))
                .responseBody(extractResponseBody(response))
                .duration((int) (System.currentTimeMillis() - startTime))
                .build();

            requestLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to save request log: {}", e.getMessage());
        }
    }

    private String extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        try {
            return objectMapper.writeValueAsString(headers);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String extractBody(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content);
            }
        }
        return "";
    }

    private String extractResponseHeaders(HttpServletResponse response) {
        Map<String, String> headers = new HashMap<>();
        for (String name : response.getHeaderNames()) {
            headers.put(name, response.getHeader(name));
        }
        try {
            return objectMapper.writeValueAsString(headers);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String extractResponseBody(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper wrapper) {
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content);
            }
        }
        return "";
    }
}