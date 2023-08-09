package com.andmark.quotebot.service;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.service.command.RequestQuoteCommand;
import com.andmark.quotebot.service.command.StartCommand;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class QuoteBot extends TelegramLongPollingCommandBot {

    @Override
    public void processNonCommandUpdate(Update update) {
        try {
            String chatId = update.getMessage().getChatId().toString();

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Hi!");

            execute(message);
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
