package com.andmark.quotebot.service.impl;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.service.*;
import com.andmark.quotebot.service.command.*;
import com.andmark.quotebot.service.enums.BotState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingCommandBot implements Bot {
    private final BotAttributes botAttributes;
    private final ApiService apiService;
    private final QuoteService quoteService;
    private final UserService userService;
    // a map to store user roles (in-memory cache)
    private final Map<Long, UserRole> userRoleCache = new ConcurrentHashMap<>();

    public TelegramBot(BotAttributes botAttributes, ApiService apiService, QuoteService quoteService, UserService userService) {
        this.botAttributes = botAttributes;
        this.apiService = apiService;
        this.quoteService = quoteService;
        this.userService = userService;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Check if the user is registered and get their role from the cache
            Message message = update.getMessage();
            Long usertgId = message.getFrom().getId();
            UserRole userRole = userRoleCache.get(usertgId);

            if (userRole == null) {
                log.debug("userRole is null");
                // Check if the user is registered
                boolean isRegistered = userService.isRegistered(usertgId);
                if (!isRegistered) {
                    log.debug("user is not registered");
                    // Initiate the registration process
                    userService.initiateRegistration(usertgId, message.getChatId());
                    return; // Exit processing for unregistered user
                }
                // If user is registered, fetch and cache their role
                userRole = apiService.getUserRole(usertgId);
                userRoleCache.put(usertgId, userRole);
            }

            quoteService.handleIncomingMessage(update);
        } else if (update.hasCallbackQuery()) {
            quoteService.handleCallbackQuery(update);
        }
    }

    @Scheduled(fixedDelay = 3_600_000) // run every hour
    public void checkAndPublishPendingQuotes() {
        log.debug("Checking for pending quotes...");
        List<QuoteDTO> pendingQuotes = apiService.getPendingQuotes();
        if (!pendingQuotes.isEmpty()) {
            log.debug("pendingQuotes size is = {}", pendingQuotes.size());
            for (QuoteDTO pendingQuote : pendingQuotes) {
                if (pendingQuote.getPendingTime() != null && pendingQuote.getPendingTime().before(new Date())) {
                    log.debug("publish quote with id = {} to group",pendingQuote.getId());
                    quoteService.publishQuoteToGroup(pendingQuote);
                }
            }
        }
    }

    @Override
    public void sendImageToChat(Long chatId, String imageUrl) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(new InputFile(imageUrl));
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            log.error("Failed to send image to chat", e);
        }
    }

    @Override
    public void sendMessage(Long chatId, InlineKeyboardMarkup keyboard, String textToSend) {
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

    @Override
    public void removeKeyboard(Long chatId) {
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
    public void sendImageAttachment(Long chatId, byte[] imageBytes, int imageNumber) {
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
        register(new HelpCommand());
        register(new SignUpCommand(userService));
        register(new YesNoMagicCommand());
        register(new QuotesWeekCommand());
        register(new StatsCommand());
        register(new VersionCommand());
        register(new RequestQuoteCommand(apiService));
    }

    @Override
    public String getBotUsername() {
        return BotConfig.botUsername;
    }

    public String getBotToken() {
        return BotConfig.botToken;
    }

    public UserRole getUserRole(Long usertgId) {
        return userRoleCache.get(usertgId);
    }

}