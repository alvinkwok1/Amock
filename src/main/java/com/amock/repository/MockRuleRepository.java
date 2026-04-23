package com.amock.repository;

import com.amock.entity.MockRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockRuleRepository extends JpaRepository<MockRule, String> {

    List<MockRule> findByEnabledTrueOrderByPriorityDesc();

    Page<MockRule> findByEnabled(Boolean enabled, Pageable pageable);

    Page<MockRule> findByMethod(String method, Pageable pageable);

    Page<MockRule> findByGroupId(String groupId, Pageable pageable);

    @Query("SELECT r FROM MockRule r WHERE " +
           "(:enabled IS NULL OR r.enabled = :enabled) AND " +
           "(:method IS NULL OR r.method = :method) AND " +
           "(:groupId IS NULL OR r.groupId = :groupId)")
    Page<MockRule> findByFilters(Boolean enabled, String method, String groupId, Pageable pageable);

    long countByGroupId(String groupId);

    void deleteByGroupId(String groupId);
}