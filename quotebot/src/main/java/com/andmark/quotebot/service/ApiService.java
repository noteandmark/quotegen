package com.andmark.quotebot.service;

import com.andmark.quotebot.dto.QuoteDTO;

import java.util.List;

public interface ApiService {
    QuoteDTO getNextQuote();
    List<QuoteDTO> getPendingQuotes();
}
