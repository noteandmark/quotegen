package com.andmark.qoutegen.service;

import com.andmark.qoutegen.dto.QuoteDTO;

public interface QuoteService extends AbstractService<QuoteDTO>{
    void checkAndPopulateCache();
    QuoteDTO provideQuoteToClient();
    void confirmQuote(Long quoteId);
    void populateCache(Integer cacheSize);
}
