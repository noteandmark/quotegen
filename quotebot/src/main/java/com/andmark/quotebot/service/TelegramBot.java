package com.andmark.quotebot.service;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.service.command.*;
import com.andmark.quotebot.service.enums.BotState;
import com.andmark.quotebot.util.BotAttributes;
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
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
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
            User user = message.getFrom();
            String userName = user.getUserName();
            log.debug("processNonCommandUpdate with user id = {}, name = {}", usertgId, userName);

            UserRole userRole = userRoleCache.get(usertgId);
            if (userRole == null) {
                log.debug("userRole is null");
                BotState state = BotAttributes.getUserCurrentBotState(usertgId);
//                Boolean checkReg = false;
                switch (state) {
                    case AWAITING_USERNAME_INPUT -> quoteService.handleUsernameInputResponse(update);
                    case AWAITING_PASSWORD_INPUT -> quoteService.handlePasswordInputResponse(update);
                    case AWAITING_REPORT -> quoteService.handleReportInput(update.getMessage().getFrom(), message.getText());
//                    default -> checkReg = registrateUser(usertgId, message);
                    default -> {
                        return;
                    }
                }
//                if (!checkReg) return; // Exit processing for unregistered user
            }

            quoteService.handleIncomingMessage(update);
        } else if (update.hasCallbackQuery()) {
            quoteService.handleCallbackQuery(update);
        }
    }

    private boolean registrateUser(Long usertgId, Message message) {
        UserRole userRole;
        // Check if the user is registered
        boolean isRegistered = userService.isRegistered(usertgId);
        if (!isRegistered) {
            log.debug("user is not registered");
            // Initiate the registration process
            userService.initiateRegistration(message.getChatId(), usertgId);
            return false;
        }
        // If user is registered, fetch and cache their role
        userRole = apiService.getUserRole(usertgId);
        userRoleCache.put(usertgId, userRole);
        return true;
    }

    // publishes pending quotes, run every hour (3600000)
    @Scheduled(fixedDelay = 3_600_000)
    public void checkAndPublishPendingQuotes() {
        log.debug("Time: {} .Checking for pending quotes...", new Date());
        List<QuoteDTO> pendingQuotes = apiService.getPendingQuotes();
        if (!pendingQuotes.isEmpty()) {
            log.debug("pendingQuotes size is = {}", pendingQuotes.size());
            for (QuoteDTO pendingQuote : pendingQuotes) {
                LocalDateTime pendingTime = pendingQuote.getPendingTime();
                LocalDateTime currentDateTime = LocalDateTime.now();

                if (pendingQuote.getPendingTime() != null && pendingTime.isBefore(currentDateTime)) {
                    log.debug("publish quote with id = {} to group", pendingQuote.getId());
                    pendingQuote = quoteService.publishQuoteToGroup(pendingQuote);
                    // save in database
                    quoteService.sendQuoteSavedTODatabase(pendingQuote, "отложенная цитата id = "
                            + pendingQuote.getId() + " опубликована");
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
        register(new StartCommand(apiService));
        register(new HelpCommand());
        register(new StatsCommand(apiService));
        register(new YesNoMagicCommand(apiService));
        register(new DivinationCommand(apiService));
        register(new GetQuoteCommand(apiService));
        register(new QuotesWeekCommand(apiService));
        register(new VersionCommand());
        register(new SignUpCommand(userService, botAttributes));
        register(new SignOutCommand(userService));
        register(new ResetCommand(botAttributes));
        register(new ReportCommand());
        register(new ScanBooksCommand(apiService));
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