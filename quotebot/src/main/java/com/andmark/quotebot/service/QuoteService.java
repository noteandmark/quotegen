package com.andmark.quotebot.service;

import com.andmark.quotebot.dto.QuoteDTO;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface QuoteService {
    void handleIncomingMessage(Update update);

    void handleCallbackQuery(Update update);

    QuoteDTO publishQuoteToGroup(QuoteDTO pendingQuote);
}
