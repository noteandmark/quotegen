package com.andmark.quotegen.repository;

import com.andmark.quotegen.domain.ScheduledActionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledActionStatusRepository extends JpaRepository<ScheduledActionStatus, Long> {
}
