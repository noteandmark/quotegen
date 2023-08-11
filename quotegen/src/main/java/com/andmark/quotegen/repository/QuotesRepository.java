package com.andmark.quotegen.repository;

import com.andmark.quotegen.domain.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotesRepository extends JpaRepository<Quote, Long> {

    int countByUsedAtIsNull();

    Quote findFirstByUsedAtIsNull();
}
