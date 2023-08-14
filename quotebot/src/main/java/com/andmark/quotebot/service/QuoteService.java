package com.andmark.quotebot.service;

import com.andmark.quotebot.dto.QuoteDTO;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface QuoteService {
    void handleIncomingMessage(Message message);

    void handleCallbackQuery(CallbackQuery callbackQuery);

    QuoteDTO publishQuoteToGroup(QuoteDTO pendingQuote);
}
