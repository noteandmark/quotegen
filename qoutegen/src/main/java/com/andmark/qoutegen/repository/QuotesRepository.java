package com.andmark.qoutegen.repository;

import com.andmark.qoutegen.models.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotesRepository extends JpaRepository<Quote, Long> {
}
