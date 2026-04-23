package com.amock.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConditionMatcher {

    private final ObjectMapper objectMapper;

    public boolean matches(String conditionsJson, HttpServletRequest request) {
        try {
            JsonNode conditions = objectMapper.readTree(conditionsJson);
            return evaluateConditions(conditions, request);
        } catch (Exception e) {
            log.warn("Failed to parse conditions JSON: {}", e.getMessage());
            return true;
        }
    }

    private boolean evaluateConditions(JsonNode conditions, HttpServletRequest request) {
        String type = conditions.has("type") ? conditions.get("type").asText() : "and";
        JsonNode rules = conditions.get("rules");

        if (rules == null || !rules.isArray()) {
            return true;
        }

        boolean result;
        if ("and".equalsIgnoreCase(type)) {
            result = true;
            for (JsonNode rule : rules) {
                if (!evaluateRule(rule, request)) {
                    result = false;
                    break;
                }
            }
        } else {
            result = false;
            for (JsonNode rule : rules) {
                if (evaluateRule(rule, request)) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    private boolean evaluateRule(JsonNode rule, HttpServletRequest request) {
        String field = rule.get("field").asText();
        String op = rule.get("op").asText();
        String value = rule.has("value") ? rule.get("value").asText() : null;

        String actualValue = extractFieldValue(field, request);

        return applyOperator(op, actualValue, value);
    }

    private String extractFieldValue(String field, HttpServletRequest request) {
        if (field.startsWith("header.")) {
            String headerName = field.substring(7);
            return request.getHeader(headerName);
        } else if (field.startsWith("query.")) {
            String paramName = field.substring(6);
            return request.getParameter(paramName);
        } else if (field.startsWith("body.")) {
            String bodyPath = field.substring(5);
            return extractBodyField(bodyPath, request);
        }
        return null;
    }

    private String extractBodyField(String path, HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length > 0) {
                try {
                    JsonNode body = objectMapper.readTree(content);
                    JsonNode field = body.get(path);
                    if (field != null) {
                        return field.asText();
                    }
                } catch (Exception e) {
                    log.debug("Failed to parse request body: {}", e.getMessage());
                }
            }
        }
        return null;
    }

    private boolean applyOperator(String op, String actualValue, String expectedValue) {
        switch (op.toLowerCase()) {
            case "eq":
                return actualValue != null && actualValue.equals(expectedValue);
            case "neq":
                return actualValue == null || !actualValue.equals(expectedValue);
            case "exists":
                return actualValue != null;
            case "contains":
                return actualValue != null && actualValue.contains(expectedValue);
            case "regex":
                if (actualValue == null) return false;
                return Pattern.matches(expectedValue, actualValue);
            default:
                log.warn("Unknown operator: {}", op);
                return false;
        }
    }
}