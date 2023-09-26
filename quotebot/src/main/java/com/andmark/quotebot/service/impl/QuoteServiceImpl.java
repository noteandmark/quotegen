package com.andmark.quotebot.service.impl;

import com.andmark.quotebot.domain.RequestConfiguration;
import com.andmark.quotebot.domain.enums.QuoteStatus;
import com.andmark.quotebot.dto.AvailableDaysResponseDTO;
import com.andmark.quotebot.dto.ExtractedLinesDTO;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.dto.UserDTO;
import com.andmark.quotebot.exception.QuoteException;
import com.andmark.quotebot.service.*;
import com.andmark.quotebot.service.enums.BotState;
import com.andmark.quotebot.service.googleapi.GoogleCustomSearchService;
import com.andmark.quotebot.service.keyboard.QuoteKeyboardService;
import com.andmark.quotebot.util.BotAttributes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static com.andmark.quotebot.config.BotConfig.*;

@Service
@Slf4j
public class QuoteServiceImpl implements QuoteService {
    private static final int IMAGE_COUNT_TO_CHOICE = 10;

    private final Bot telegramBot;
    private final BotAttributes botAttributes;
    private final ApiService apiService;
    private final UserService userService;
    private final GoogleCustomSearchService googleCustomSearchService;
    private final QuoteKeyboardService quoteKeyboardService;
    private final UserRegistrationService userRegistrationService;

    private Stack<String> quoteText;

    public QuoteServiceImpl(@Lazy Bot telegramBot, BotAttributes botAttributes, ApiService apiService, UserService userService, GoogleCustomSearchService googleCustomSearchService, QuoteKeyboardService quoteKeyboardService, UserRegistrationService userRegistrationService) {
        this.telegramBot = telegramBot;
        this.botAttributes = botAttributes;
        this.apiService = apiService;
        this.userService = userService;
        this.googleCustomSearchService = googleCustomSearchService;
        this.quoteKeyboardService = quoteKeyboardService;
        this.userRegistrationService = userRegistrationService;
        //a cache to store edited text
        quoteText = new Stack<>();
    }

    @Override
    public void handleIncomingMessage(Update update) {
        Message message = update.getMessage();
        log.debug("handleIncomingMessage in chatId: {}, messageId: {}", message.getChatId(), message.getMessageId());
        String userInput = message.getText().trim();
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();
        BotState currentState = BotAttributes.getUserCurrentBotState(userId);
        log.debug("userInput = {}, botState = {}", userInput, currentState);

        if (userInput.equals("сброс")) {
            clearBotAttributes(userId);
            telegramBot.sendMessage(chatId, null, "Состояние сброшено");
            log.debug("reset made after input 'сброс'");
            return;
        }

        if (userId.equals(adminChatId) && userInput.startsWith("q:")) {
            log.debug("quoteText offer text = {}", userInput);
            quoteText.push(userInput.substring(2));
        } else {
            switch (currentState) {
                case AWAITING_IMAGE_CHOICE -> handleImageChoiceResponse(chatId, userInput);
                case AWAITING_PUBLISHING -> handlePublishingChoiceResponse(chatId, userInput);
                case POSTPONE -> handlePostponeChoiceResponse(chatId, userInput);
                case AWAITING_PAGE_NUMBER -> handlePageNumberInput(userId, userInput);
                case AWAITING_LINE_NUMBER -> handleLineNumberInput(userId, userInput);
                default -> log.warn("state encountered: {}", currentState);
            }
        }
    }

    @Override
    public void handleCallbackQuery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        log.debug("chatId = {}, callbackQuery text() = {}", callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getText());
        botAttributes.setLastMessageId(callbackQuery.getMessage().getMessageId());
        log.debug("handleCallbackQuery with lastMessageId: {}", botAttributes.getLastMessageId());
        botAttributes.setLastCallbackMessage(callbackQuery.getMessage().getText());
        log.debug("botAttributes setLastCallbackMessage");
        Long chatId = callbackQuery.getMessage().getChatId();
        Long userId = callbackQuery.getFrom().getId();
        log.debug("chatID = {}", chatId);
        BotState currentBotState = BotAttributes.getUserCurrentBotState(userId);

        String[] dataParts = callbackQuery.getData().split("-");
        String action = dataParts[0];
        Long quoteId = Long.valueOf(dataParts[1]);
        log.debug("action = {}  quoteId = {}", action, quoteId);
        log.debug("botAttributes.getCurrentState() = {}", currentBotState);
        if (currentBotState.equals(BotState.START)) {
            switch (action) {
                case "edit" -> editQuote(chatId, quoteId);
                case "confirm" -> confirmQuote(chatId, quoteId);
                case "reject" -> rejectQuote(chatId, quoteId);
                default -> log.warn("no action");
            }
        }
    }

    public QuoteDTO publishQuoteToGroup(QuoteDTO quoteDTO) {
        log.debug("quote id = {} service publish to group", quoteDTO.getId());

        boolean hasContent = quoteDTO.getContent() != null && !quoteDTO.getContent().isEmpty();
        boolean hasImage = quoteDTO.getImageUrl() != null && !quoteDTO.getImageUrl().isEmpty();

        if (hasContent) {
            String randomGreeting = apiService.getRandomGreeting();
            String signature = "\nВаш бот Книголюб, " + botUsername;
            if (!randomGreeting.isEmpty()) {
                log.debug("sending greeting to user");
                telegramBot.sendMessage(groupChatId, null, randomGreeting + signature);
            }
            log.debug("sending message with content quote id = {} to group chat", quoteDTO.getId());
            telegramBot.sendMessage(groupChatId, null, quoteDTO.getContent());

            if (hasImage) {
                log.debug("sending message with image quote id = {} to group chat", quoteDTO.getId());
                telegramBot.sendImageToChat(groupChatId, quoteDTO.getImageUrl());
            }
            quoteDTO.setStatus(QuoteStatus.PUBLISHED);

        }
        BotAttributes.setUserCurrentBotState(adminChatId, BotState.START);
        return quoteDTO;
    }

    public void sendQuoteSavedTODatabase(QuoteDTO quoteDTO, String message) {
        log.debug("send the quote to be saved in the database");
        String confirmUrl = API_BASE_URL + "/quotes/confirm";
        RequestConfiguration requestConfig = new RequestConfiguration.Builder()
                .url(confirmUrl)
                .httpMethod(HttpMethod.POST)
                .requestBody(quoteDTO)
                .chatId(adminChatId)
                .successMessage(message)
                .keyboard(null)
                .build();
        apiService.sendRequestAndHandleResponse(requestConfig);
    }

    void handleImageChoiceResponse(Long chatId, String userInput) {
        log.debug("current state is AWAITING_IMAGE_CHOICE");
        String chosenImageUrl = null;

        if (isValidImageChoice(userInput, botAttributes.getImageUrls().size())) {
            if (!userInput.equals("0")) {
                chosenImageUrl = botAttributes.getImageUrls().get(Integer.parseInt(userInput) - 1);
                botAttributes.setConfirmedUrl(chosenImageUrl);
                log.debug("userInput = {}, chosenImageUrl = {}", userInput, chosenImageUrl);
            } else {
                log.debug("no chosenImageUrl, set null");
                botAttributes.setConfirmedUrl(null);
            }
        } else {
            log.warn("NumberFormatException in NumberFormatException with choice = {}", userInput);
            telegramBot.sendMessage(chatId, null, "Выбери изображение от 0 до 10 (0 - пост без изображений).");
            return;
        }
        postingQuote(chatId);
    }

    void postingQuote(Long chatId) {
        int lastCallbackMessageLength = botAttributes.getLastCallbackMessage().length();
        BotState currentBotState = BotAttributes.getUserCurrentBotState(adminChatId);
        log.debug("in postingQuote with lastCallbackMessage length: {}, chosenImageUrl: {}",
                lastCallbackMessageLength, botAttributes.getConfirmedUrl());
        // choose whether to publish the quote immediately or postpone it
        log.debug("in postingQuote currentState = {}", currentBotState);
        telegramBot.sendMessage(chatId, null, "Публиковать [сразу] или [отложить]?");
        BotAttributes.setUserCurrentBotState(adminChatId, BotState.AWAITING_PUBLISHING);
    }

    void handlePublishingChoiceResponse(Long chatId, String userInput) {
        log.debug("current state is AWAITING_PUBLISHING, userInput = {}", userInput);
        switch (userInput) {
            case "сразу" -> {
                // publishing a post in a telegram group
                QuoteDTO quoteDTO = new QuoteDTO();
                quoteDTO.setId(botAttributes.getQuoteId());
                quoteDTO.setContent(botAttributes.getConfirmedContent());
                if (botAttributes.getConfirmedUrl() != null) {
                    quoteDTO.setImageUrl(botAttributes.getConfirmedUrl());
                }
                quoteDTO = publishQuoteToGroup(quoteDTO);
                // send the quote to be saved in the database
                sendQuoteSavedTODatabase(quoteDTO, "пост опубликован сразу");
            }
            case "отложить" -> {
                log.debug("case 'отложить'");
                telegramBot.sendMessage(chatId, null, "Напиши дату публикации в виде: [yyyy-MM-dd HH:mm:ss] или напиши [случайно]");
                BotAttributes.setUserCurrentBotState(adminChatId, BotState.POSTPONE);
            }
            default -> {
                log.debug("case default, userInput in handlePublishingChoiceResponse = {}", userInput);
                if (!isValidImageChoice(userInput, botAttributes.getImageUrls().size())) {
                    telegramBot.sendMessage(chatId, null, "Напиши выбор в виде: [сразу] или [отложить]");
                }
            }
        }
    }

    private void handlePostponeChoiceResponse(Long chatId, String userInput) {
        log.debug("current state is POSTPONE");
        // random selection of publication date
        if (userInput.equalsIgnoreCase("случайно")) {
            log.debug("quote service: handlePostponeChoiceResponse user input = 'случайно'");
            // run logic of random publication
            scheduleRandomPendingPublication();
        }
        // selection for a specific date and time
        else {
            // Parse the input as a date
            try {
                LocalDateTime pendingTime = LocalDateTime.parse(userInput, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                // send the quote to be saved in the database
                log.debug("sent quote with pendingTime = {} to be saved in database", pendingTime);
                postponePublishing(pendingTime);
            } catch (DateTimeParseException e) {
                // Handle invalid date format error
                if (!userInput.equals("отложить"))
                    telegramBot.sendMessage(chatId, null, "Неверный формат. Напиши в формате: [yyyy-MM-dd HH:mm:ss] или: [случайно].");
            }
        }
    }

    void scheduleRandomPendingPublication() {
        log.debug("random pending publication");
        AvailableDaysResponseDTO availableDays = apiService.findAvailableDays();
        LocalDateTime availableDay = availableDays.getAvailableDay();
        String message = availableDays.getMessage();

        if (availableDay != null) {
            // The availableDay field is not null
            log.debug("availableDay is LocalDateTime");
            postponePublishing(availableDay);
        } else {
            // The availableDay field is null or doesn't contain a valid date-time value
            log.debug("availableDay is null");
            BotAttributes.setUserCurrentBotState(adminChatId, BotState.POSTPONE);
            log.debug("set state = {} to user id = {}", BotAttributes.getUserCurrentBotState(adminChatId), adminChatId);
        }
        telegramBot.sendMessage(adminChatId, null, message);
    }

    void postponePublishing(LocalDateTime pendingTime) {
        log.debug("postponePublishing with pendingTime = {}", pendingTime);
        QuoteDTO quoteDTO = new QuoteDTO();
        quoteDTO.setId(botAttributes.getQuoteId());
        quoteDTO.setContent(botAttributes.getConfirmedContent());
        quoteDTO.setPendingTime(pendingTime);
        quoteDTO.setStatus(QuoteStatus.PENDING);
        quoteDTO.setImageUrl(botAttributes.getConfirmedUrl());

        String confirmUrl = API_BASE_URL + "/quotes/pending";
        RequestConfiguration requestConfig = new RequestConfiguration.Builder()
                .url(confirmUrl)
                .httpMethod(HttpMethod.POST)
                .requestBody(quoteDTO)
                .chatId(adminChatId)
                .successMessage("успешно отложена на " + pendingTime)
                .keyboard(null)
                .build();
        apiService.sendRequestAndHandleResponse(requestConfig);
        clearBotAttributes(adminChatId);
    }

    void editQuote(Long chatId, Long quoteId) {
        log.debug("editQuote with chatId: {} and quoteId: {}", chatId, quoteId);
        // Edit the original message to remove the inline keyboard
        telegramBot.removeKeyboard(chatId);

        InlineKeyboardMarkup keyboard = quoteKeyboardService.getEditKeyboardMarkup(quoteId);
        String editedText = (!quoteText.isEmpty()) ? quoteText.peek() : "write quoteText starting with q:";
        log.debug("editedText = {}", editedText);
        telegramBot.sendMessage(chatId, keyboard, editedText);
    }

    void confirmQuote(Long chatId, Long quoteId) {
        log.debug("confirmQuote with chatId: {} and quoteId: {} and quoteText.isEmpty: {}", chatId, quoteId, quoteText.isEmpty());
        String content = (!quoteText.isEmpty()) ? quoteText.pop() : botAttributes.getLastCallbackMessage();
        botAttributes.setConfirmedContent(content);
        // searching images for content with Google custom search api
        log.debug("content = {}", content);
        // limit the length of the get request
        String contentRequest = content.substring(0, Math.min(content.length(), 1024));
        botAttributes.setImageUrls(googleCustomSearchService.searchImagesByKeywords(contentRequest));
        log.debug("size of botAttributes.setImageUrls = {}", botAttributes.getImageUrls().size());
        botAttributes.setQuoteId(quoteId);
        // sending selectable images to the user
        sendImagesToChoice(chatId);
    }

    void rejectQuote(Long chatId, Long quoteId) {
        log.debug("rejectQuote with chatId: {} and quoteId: {}", chatId, quoteId);
        String rejectUrl = API_BASE_URL + "/quotes/reject?id=" + quoteId;
        log.debug("rejectUrl quote with url = {}", rejectUrl);

        RequestConfiguration requestConfig = new RequestConfiguration.Builder()
                .url(rejectUrl)
                .httpMethod(HttpMethod.DELETE)
                .requestBody(null)
                .chatId(adminChatId)
                .successMessage("успешно удалена")
                .keyboard(null)
                .build();
        apiService.sendRequestAndHandleResponse(requestConfig);
    }

    void sendImagesToChoice(Long chatId) {
        for (int i = 0; i < IMAGE_COUNT_TO_CHOICE; i++) {
            log.debug("in sendImagesToChoice botAttributes.getImageUrls().size() = {}", botAttributes.getImageUrls().size());
            if (botAttributes.getImageUrls().size() > i) {
                String imageUrl = botAttributes.getImageUrls().get(i);
                byte[] imageBytes = apiService.downloadImage(imageUrl);
                // Send the image as an attachment
                telegramBot.sendImageAttachment(chatId, imageBytes, i + 1);
            }
        }
        // ask for image choice
        telegramBot.removeKeyboard(chatId);
        telegramBot.sendMessage(chatId, null, "Выбери изображение, введя цифру в пределах [1-" + botAttributes.getImageUrls().size() + "]"
                + "\n" + "Или введи [0] для поста без картинки");
        BotAttributes.setUserCurrentBotState(adminChatId, BotState.AWAITING_IMAGE_CHOICE);
    }

    private boolean isValidImageChoice(String userInput, int countImages) {
        try {
            int choice = Integer.parseInt(userInput);
            return choice >= 0 && choice <= countImages;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void handleUsernameInputResponse(Update update) {
        log.debug("QuoteService in handleUsernameInputResponse");
        String username = update.getMessage().getText().trim();
        Long chatId = update.getMessage().getChatId();
        // Get the user ID of the current user
        User user = update.getMessage().getFrom();
        Long usertgId = user.getId();
        // Forward the response to the UserRegistrationService if username isn't taken
        if (!userService.isUsernameTaken(username)) {
            log.debug("username is available, proceed with registration");
            userRegistrationService.handleUsernameInput(usertgId, chatId, username);
            BotAttributes.setUserCurrentBotState(usertgId, BotState.AWAITING_PASSWORD_INPUT);
        } else {
            log.warn("username is already taken");
            telegramBot.sendMessage(chatId, null, "Имя пользователя уже занято. Пожалуйста, выберите другое имя.");
        }
    }

    public void handlePasswordInputResponse(Update update) {
        log.debug("QuoteService in handlePasswordInputResponse");
        String password = update.getMessage().getText().trim();
        Long chatId = update.getMessage().getChatId();
        User user = update.getMessage().getFrom();
        Long usertgId = user.getId();
        // Forward the response to the UserRegistrationService
        UserDTO userDTO = userRegistrationService.handlePasswordInput(usertgId, chatId, password);
        if (userDTO != null) {
            apiService.registerUser(chatId, userDTO);
            // Remove the user from the registration in progress list
            userRegistrationService.completeRegistration(usertgId, chatId);
            BotAttributes.clear(usertgId);
        } else {
            log.warn("userRegistrationService.handlePasswordInput return null in userDTO ");
        }
    }

    public void handlePageNumberInput(Long userId, String userInput) {
        log.debug("current state is AWAITING_PAGE_NUMBER");
        int pageNumber = 0;
        try {
            log.debug("handle pageNumber for userId = {}, userInput = {}", userId, userInput);
            pageNumber = Integer.parseInt(userInput);
        } catch (NumberFormatException e) {
            log.warn("handlePageNumberInput wrong input");
            telegramBot.sendMessage(userId, null, "Введите число!");
            throw new QuoteException("handlePageNumberInput wrong input:" + e);
        }
        // Update user's current state
        BotAttributes.setUserCurrentBotState(userId, BotState.AWAITING_LINE_NUMBER);
        // Store the page number in user's context
        botAttributes.setPageNumber(userId, pageNumber);
        log.debug("set page number = {} to userContext for userId = {}", pageNumber, userId);
        // Ask for line number
        telegramBot.sendMessage(userId, null, "Хорошо! Теперь напиши номер строки.");
    }

    public void handleLineNumberInput(Long userId, String userInput) {
        log.debug("current state is AWAITING_LINE_NUMBER");
        int lineNumber = 0;
        try {
            lineNumber = Integer.parseInt(userInput);
        } catch (NumberFormatException e) {
            log.warn("handleLineNumberInput wrong input");
            telegramBot.sendMessage(userId, null, "Введите число!");
            throw new QuoteException("handleLineNumberInput wrong input:" + e);
        }
        // Update user's current state
        log.debug("set line number = {} for userId = {}", lineNumber, userId);
        // start divination
        // Retrieve the user's page number from the UserContext
        int pageNumber = botAttributes.getPageNumber(userId);

        // Pass page and line numbers to Rest-API service
        ExtractedLinesDTO extractedLinesDTO = apiService.processPageAndLineNumber(userId, pageNumber, lineNumber);

        List<String> extractedLines = extractedLinesDTO.getLines();
        log.debug("get extractedLines, size = {}", extractedLines.size());
        // Send lines to user
        StringBuilder formattedMessage = new StringBuilder("Книга открыта на странице " + pageNumber +
                " строки, начиная с " + lineNumber + " такие:\n\n");

        for (String extractedLine : extractedLines) {
            formattedMessage.append(extractedLine).append("\n");
        }

        formattedMessage.append("\n\n").append(extractedLinesDTO.getBookAuthor()).append("\n").append(extractedLinesDTO.getBookTitle());
        log.info("sending formattedMessage with divination lines to user: {}", formattedMessage);
        telegramBot.sendMessage(userId, null, formattedMessage.toString());
        // Clear user's context after processing
        BotAttributes.clear(userId);
    }

    public void handleReportInput(User user, String reportMessage) {
        log.debug("current state is AWAITING_REPORT");
        reportMessage = "Новое сообщение от пользователя @" + user.getUserName() + ":\n" + reportMessage;
        telegramBot.sendMessage(adminChatId, null, reportMessage);
        log.info("new report from user id = {}, username = {}, message = {}", user.getId(), user.getUserName(), reportMessage);
        telegramBot.sendMessage(user.getId(), null, "Ваше сообщение отправлено.");
        BotAttributes.clear(user.getId());
    }

    private void clearBotAttributes(Long userId) {
        BotAttributes.setUserCurrentBotState(adminChatId, BotState.START);
        botAttributes.setConfirmedUrl("");
        botAttributes.setConfirmedContent("");
        botAttributes.setImageUrls(new ArrayList<>());
        log.debug("botAttributes cleared");
    }

}
