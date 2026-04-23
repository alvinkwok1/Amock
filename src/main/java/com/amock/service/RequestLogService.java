package com.amock.service;

import com.amock.dto.PageResponse;
import com.amock.dto.RequestLogResponse;
import com.amock.dto.RequestStatsResponse;
import com.amock.entity.RequestLog;
import com.amock.exception.ResourceNotFoundException;
import com.amock.repository.RequestLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestLogService {

    private final RequestLogRepository requestLogRepository;

    public PageResponse<RequestLogResponse> getRequests(String method, String path, LocalDateTime startTime, LocalDateTime endTime, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RequestLog> logs = requestLogRepository.findByFilters(method, path, startTime, endTime, pageRequest);
        Page<RequestLogResponse> responses = logs.map(RequestLogResponse::fromEntity);
        return PageResponse.fromPage(responses);
    }

    public RequestLogResponse getRequest(String id) {
        RequestLog log = requestLogRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RequestLog", id));
        return RequestLogResponse.fromEntity(log);
    }

    @Transactional
    public void clearAllRequests() {
        requestLogRepository.deleteAll();
    }

    public RequestStatsResponse getStats() {
        long totalRequests = requestLogRepository.countTotal();

        List<Object[]> methodCounts = requestLogRepository.countByMethod();
        Map<String, Long> requestsByMethod = new HashMap<>();
        for (Object[] row : methodCounts) {
            requestsByMethod.put((String) row[0], (Long) row[1]);
        }

        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        long requestsToday = requestLogRepository.countByCreatedAtAfter(todayStart);

        Double avgDuration = requestLogRepository.getAverageDurationSince(todayStart);
        if (avgDuration == null) avgDuration = 0.0;

        return new RequestStatsResponse(totalRequests, requestsByMethod, avgDuration, requestsToday);
    }

    public RequestLogResponse replayRequest(String id) {
        RequestLog log = requestLogRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RequestLog", id));
        return RequestLogResponse.fromEntity(log);
    }
}