package com.andmark.quotegen.repository;

import com.andmark.quotegen.domain.Quote;
import com.andmark.quotegen.domain.enums.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface QuotesRepository extends JpaRepository<Quote, Long> {

    int countByUsedAtIsNull();

    Quote findFirstByUsedAtIsNull();

    List<Quote> findByStatus(QuoteStatus status);

    Long countByStatus(QuoteStatus quoteStatus);

    Long countByStatusAndUsedAtAfter(QuoteStatus quoteStatus, LocalDateTime startOfYear);

    List<Quote> findByStatusAndUsedAtBetween(QuoteStatus quoteStatus, LocalDateTime startOfWeek, LocalDateTime endOfWeek);
}
