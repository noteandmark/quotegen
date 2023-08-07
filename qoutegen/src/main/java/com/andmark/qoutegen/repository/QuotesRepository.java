package com.andmark.qoutegen.repository;

import com.andmark.qoutegen.domain.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotesRepository extends JpaRepository<Quote, Long> {

    int countByUsedAtIsNull();

    Quote findFirstByUsedAtIsNull();
}
