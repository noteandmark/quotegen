package com.andmark.qoutegen.service;

import com.andmark.qoutegen.domain.Quote;
import com.andmark.qoutegen.dto.QuoteDTO;

public interface QuoteService extends AbstractService<Quote>{
    void checkAndPopulateCache();

    void waitForSuitableQuotes();

    String provideQuoteToClient();
}
