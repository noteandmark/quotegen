package com.andmark.qoutegen.service.impl;

import com.andmark.qoutegen.models.Quote;
import com.andmark.qoutegen.repository.QuotesRepository;
import com.andmark.qoutegen.service.QuoteService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@Slf4j
public class QuoteServiceImpl implements QuoteService {

    private final QuotesRepository quotesRepository;
    private final ModelMapper mapper;

    @Autowired
    public QuoteServiceImpl(QuotesRepository quotesRepository, ModelMapper mapper) {
        this.quotesRepository = quotesRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(Quote quote) {
        log.debug("saving book");
        quotesRepository.save(quote);
        log.info("save book {}", quote);
    }

    @Override
    public Quote findOne(Long id) {
        log.debug("find quote by id {}", id);
        Optional<Quote> foundQuote = quotesRepository.findById(id);
        log.info("find quote {}", foundQuote);
        return foundQuote.orElse(null);
    }

    @Override
    public List<Quote> findAll() {
        log.debug("find all quotes");
        return quotesRepository.findAll();
    }

    @Override
    @Transactional
    public void update(Long id, Quote updatedQuote) {
        log.debug("update book by id {}", id);
        updatedQuote.setId(id);
        quotesRepository.save(updatedQuote);
        log.info("update quote {}", updatedQuote);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("delete quote by id {}", id);
        quotesRepository.deleteById(id);
        log.info("delete quote with id {} perform", id);
    }
}
