package com.andmark.quotegen.repository;

import com.andmark.quotegen.domain.Quote;
import com.andmark.quotegen.domain.enums.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface QuotesRepository extends JpaRepository<Quote, Long> {

    int countByUsedAtIsNull();
    int countByUsedAtIsNullAndPendingTimeIsNull();

    Quote findFirstByUsedAtIsNull();

    List<Quote> findByStatus(QuoteStatus status);

    Long countByStatus(QuoteStatus quoteStatus);

    Long countByStatusAndUsedAtAfter(QuoteStatus quoteStatus, LocalDateTime startOfYear);

    List<Quote> findByStatusAndUsedAtBetween(QuoteStatus quoteStatus, LocalDateTime startOfWeek, LocalDateTime endOfWeek);

    // Define a query method to fetch all pending times between startTime and endTime
    @Query("SELECT q.pendingTime FROM Quote q WHERE q.pendingTime BETWEEN :startTime AND :endTime")
    List<LocalDateTime> findPendingTimesBetween(@Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

}
