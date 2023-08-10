package com.andmark.quotebot.service;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.service.command.RatifyQuote;
import com.andmark.quotebot.service.command.RequestQuoteCommand;
import com.andmark.quotebot.service.command.StartCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class QuoteBot extends TelegramLongPollingCommandBot {
    private final RatifyQuote ratifyQuote;

    public QuoteBot(RatifyQuote ratifyQuote) {
        this.ratifyQuote = ratifyQuote;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        try {
            SendMessage message;
            if (update.hasMessage()) {
                String chatId = update.getMessage().getChatId().toString();

                message = new SendMessage();
                message.setChatId(chatId);
                message.setText("Hi!");

                execute(message);
            } else if (update.hasCallbackQuery()) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                log.debug("update.hasCallbackQuery() = {}", callbackQuery.getMessage().toString());
                ratifyQuote.handleCallbackQuery(callbackQuery);

            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRegister() {
        register(new StartCommand());
        register(new RequestQuoteCommand());
    }

    @Override
    public String getBotUsername() {
        return BotConfig.botUsername;
    }

    public String getBotToken() {
        return BotConfig.botToken;
    }
}
