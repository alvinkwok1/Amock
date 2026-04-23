package com.amock.repository;

import com.amock.entity.RuleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleGroupRepository extends JpaRepository<RuleGroup, String> {
}