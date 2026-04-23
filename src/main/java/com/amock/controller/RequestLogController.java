package com.amock.controller;

import com.amock.dto.PageResponse;
import com.amock.dto.RequestLogResponse;
import com.amock.dto.RequestStatsResponse;
import com.amock.service.RequestLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestLogController {

    private final RequestLogService requestLogService;

    @GetMapping
    public PageResponse<RequestLogResponse> getRequests(
        @RequestParam(required = false) String method,
        @RequestParam(required = false) String path,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size) {
        return requestLogService.getRequests(method, path, startTime, endTime, page, size);
    }

    @GetMapping("/{id}")
    public RequestLogResponse getRequest(@PathVariable String id) {
        return requestLogService.getRequest(id);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearAllRequests() {
        requestLogService.clearAllRequests();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public RequestStatsResponse getStats() {
        return requestLogService.getStats();
    }

    @PostMapping("/{id}/replay")
    public RequestLogResponse replayRequest(@PathVariable String id) {
        return requestLogService.replayRequest(id);
    }
}