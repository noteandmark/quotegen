package com.andmark.qoutegen.service;

import com.andmark.qoutegen.domain.Quote;

public interface QuoteService extends AbstractService<Quote>{
    void checkAndPopulateCache();

    void waitForSuitableQuotes();

    Quote provideQuoteToClient();
}
