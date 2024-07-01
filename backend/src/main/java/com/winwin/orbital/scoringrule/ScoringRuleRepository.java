package com.winwin.orbital.scoringrule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoringRuleRepository extends JpaRepository<ScoringRule, Long> {
}
