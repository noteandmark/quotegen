package com.andmark.quotebot.service;

import com.andmark.quotebot.dto.QuoteDTO;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

public interface QuoteService {
    void handleIncomingMessage(Update update);

    void handleCallbackQuery(Update update);

    QuoteDTO publishQuoteToGroup(QuoteDTO pendingQuote);

    void handleUsernameInputResponse(Update update);

    void handlePasswordInputResponse(Update update);

    void sendQuoteSavedTODatabase(QuoteDTO pendingQuote, String s);

    void handleReportInput(User from, String text);
}
