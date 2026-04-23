package com.amock.repository;

import com.amock.entity.RequestLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RequestLogRepository extends JpaRepository<RequestLog, String> {

    Page<RequestLog> findByMethod(String method, Pageable pageable);

    Page<RequestLog> findByPathContaining(String path, Pageable pageable);

    @Query("SELECT r FROM RequestLog r WHERE " +
           "(:method IS NULL OR r.method = :method) AND " +
           "(:path IS NULL OR r.path LIKE CONCAT('%', :path, '%')) AND " +
           "(:startTime IS NULL OR r.createdAt >= :startTime) AND " +
           "(:endTime IS NULL OR r.createdAt <= :endTime)")
    Page<RequestLog> findByFilters(String method, String path, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    @Modifying
    @Query("DELETE FROM RequestLog r WHERE r.createdAt < :cutoffTime")
    int deleteByCreatedAtBefore(LocalDateTime cutoffTime);

    @Modifying
    @Query("DELETE FROM RequestLog")
    void deleteAll();

    @Query("SELECT COUNT(r) FROM RequestLog r")
    long countTotal();

    @Query("SELECT r.method, COUNT(r) as count FROM RequestLog r GROUP BY r.method")
    List<Object[]> countByMethod();

    @Query("SELECT COUNT(r) FROM RequestLog r WHERE r.createdAt >= :startTime")
    long countByCreatedAtAfter(LocalDateTime startTime);

    @Query("SELECT AVG(r.duration) FROM RequestLog r WHERE r.createdAt >= :startTime")
    Double getAverageDurationSince(LocalDateTime startTime);
}