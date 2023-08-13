package com.andmark.quotegen.service;

import com.andmark.quotegen.dto.QuoteDTO;

import java.util.List;

public interface QuoteService extends AbstractService<QuoteDTO>{
    void checkAndPopulateCache();
    QuoteDTO provideQuoteToClient();
    void confirmQuote(QuoteDTO quoteDTO);
    void populateCache(Integer cacheSize);
    List<QuoteDTO> getPendingQuotes();
    void pendingQuote(QuoteDTO quoteDTO);
}
