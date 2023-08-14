package com.andmark.quotebot.service;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.service.command.RequestQuoteCommand;
import com.andmark.quotebot.service.command.StartCommand;
import com.andmark.quotebot.service.enums.BotState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.util.*;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingCommandBot implements Bot{
    private final BotAttributes botAttributes;
    private final ApiService apiService;
    private final QuoteService quoteService;

    public TelegramBot(BotAttributes botAttributes, ApiService apiService, QuoteService quoteService) {
        this.botAttributes = botAttributes;
        this.apiService = apiService;
        this.quoteService = quoteService;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            quoteService.handleIncomingMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            quoteService.handleCallbackQuery(update.getCallbackQuery());
        }
    }

    @Scheduled(fixedDelay = 3_600_000) // run every hour
    public void checkAndPublishPendingQuotes() {
        log.debug("Checking for pending quotes...");
        List<QuoteDTO> pendingQuotes = apiService.getPendingQuotes();
        if (!pendingQuotes.isEmpty()) {
            for (QuoteDTO pendingQuote : pendingQuotes) {
                if (pendingQuote.getPendingTime() != null && pendingQuote.getPendingTime().before(new Date())) {
                    quoteService.publishQuoteToGroup(pendingQuote);
                }
            }

        }
    }

    @Override
    public void sendImageToChat(String chatId, String imageUrl) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(new InputFile(imageUrl));
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            log.error("Failed to send image to chat", e);
        }
    }

    public void sendMessage(String chatId, InlineKeyboardMarkup keyboard, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(Long.valueOf(chatId));
        if (keyboard != null) {
            sendMessage.setReplyMarkup(keyboard);
        }
        sendMessage.setText(textToSend);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to send message with text = {}", textToSend);
        }
    }

    public void removeKeyboard(String chatId) {
        EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup();
        editMessage.setChatId(Long.valueOf(chatId));
        editMessage.setMessageId(botAttributes.getLastMessageId());
        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            log.error("Can't remove the inline keyboard in messageId = {}", botAttributes.getLastMessageId());
        }
    }

    @Override
    public void sendImageAttachment(String chatId, byte[] imageBytes, int imageNumber) {
        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(chatId)
                .photo(new InputFile(new ByteArrayInputStream(imageBytes), "image.jpg"))
                .caption("Image " + imageNumber)
                .build();
        try {
            execute(sendPhoto);
            log.debug("execute sendPhoto");
        } catch (TelegramApiException e) {
            log.warn("error TelegramApiException in sendImageAttachment");
        }
    }

    @Override
    public void onRegister() {
        botAttributes.setCurrentState(BotState.FREE_STATE);
        register(new StartCommand());
        register(new RequestQuoteCommand(apiService));
    }

    @Override
    public String getBotUsername() {
        return BotConfig.botUsername;
    }

    public String getBotToken() {
        return BotConfig.botToken;
    }

}