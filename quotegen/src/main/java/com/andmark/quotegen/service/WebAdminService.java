package com.andmark.quotegen.service;

import com.andmark.quotegen.dto.QuoteDTO;

public interface WebAdminService {
    void randomPublish(QuoteDTO pendingQuote);
}
