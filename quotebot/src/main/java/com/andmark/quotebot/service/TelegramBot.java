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
    private final UserRoleService userRoleService;
    private final ScheduledQuoteSenderService scheduledQuoteSenderService;
    // a map to store user roles (in-memory cache)
    private final Map<Long, UserRole> userRoleCache = new ConcurrentHashMap<>();

    public TelegramBot(BotAttributes botAttributes,
                       ApiService apiService,
                       QuoteService quoteService,
                       UserService userService, UserRoleService userRoleService,
                       ScheduledQuoteSenderService scheduledQuoteSenderService) {
        this.botAttributes = botAttributes;
        this.apiService = apiService;
        this.quoteService = quoteService;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.scheduledQuoteSenderService = scheduledQuoteSenderService;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Check if the user is registered and get their role from the cache
            Message message = update.getMessage();
            Long usertgId = message.getFrom().getId();
            User user = message.getFrom();
            String userName = user.getUserName();
            log.debug("processNonCommandUpdate with user id = {}, name = {}, botState = {}", usertgId, userName, BotAttributes.getUserCurrentBotState(usertgId));

            // Check if the user is registered and in cache memory
            UserRole userRole = userRoleCache.get(usertgId);
            // no in cache memory, check in database
            if (userRole == null) {
                log.debug("user role from cache is null");
                boolean isRegistered = userService.isRegistered(usertgId);
                log.debug("isRegistered = {}", isRegistered);
                // the user has not yet registered anywhere, can only send a message in these cases:
                if (!isRegistered) {
                    log.debug("user is not registered");
                    // check the status of a task for a specific Telegram user
                    BotState state = BotAttributes.getUserCurrentBotState(usertgId);
                    log.debug("state = {}", state);
                    final User fromUser = update.getMessage().getFrom();
                    final String messageText = message.getText();
                    switch (state) {
                        case AWAITING_USERNAME_INPUT -> quoteService.handleUsernameInputResponse(update);
                        case AWAITING_PASSWORD_INPUT -> quoteService.handlePasswordInputResponse(update);
                        case AWAITING_REPORT ->
                                quoteService.handleReportInput(fromUser, messageText);
                        default -> {
                            log.debug("the bot is silent");
                            return;
                        }
                    }
                }
                // user found in the database, temporarily stores
                else {
                    log.debug("getting registered user role");
                    userRole = apiService.getUserRole(usertgId);
                    userRoleCache.put(usertgId, userRole);
                    // processing his message
                }
            }
            log.debug("permit handle incoming message");
            quoteService.handleIncomingMessage(update);
        } else if (update.hasCallbackQuery()) {
            quoteService.handleCallbackQuery(update);
        }
    }

    // Scheduled to run at 9 am, 12 pm, 3 pm, 6 pm, and 9 pm:
    // @Scheduled(cron = "0 0 9,12,15,18,21 * * *")
    // run every hour (3600000 sec.):
    @Scheduled(fixedDelay = 3_600_000)
    public void scheduledOperations() {
        // publishes pending quotes
        checkAndPublishPendingQuotes();
        // send next quote to admin with a period of time
        scheduledQuoteSenderService.sendQuoteToAdmin();
    }

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
        sendMessage.setChatId(chatId);
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
        register(new SuggestQuoteCommand(apiService));
        register(new WebLinkCommand(apiService));
        register(new ReadmeCommand());
        register(new SignUpCommand(userService, botAttributes));
        register(new SignOutCommand(userService));
        register(new ResetCommand(botAttributes));
        register(new ReportCommand());
        register(new ScanBooksCommand(apiService));
        register(new PendingQuotesCommand(apiService, userRoleService));
        register(new RequestQuoteCommand(apiService, userRoleService));
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