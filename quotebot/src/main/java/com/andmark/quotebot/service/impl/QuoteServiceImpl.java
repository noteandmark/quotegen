package com.andmark.quotebot.service.impl;

import com.andmark.quotebot.domain.enums.QuoteStatus;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.dto.UserDTO;
import com.andmark.quotebot.service.*;
import com.andmark.quotebot.service.enums.BotState;
import com.andmark.quotebot.service.googleapi.GoogleCustomSearchService;
import com.andmark.quotebot.service.keyboard.QuoteKeyboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

        log.debug("text = {}", userInput);
        if (userInput.equals("сброс")) {
            clearBotAttributes();
            telegramBot.sendMessage(chatId, null, "Состояние сброшено");
        }
        if (userInput.startsWith("q:")) {
            quoteText.push(userInput.substring(2));
            log.debug("quoteText offer text = {}", userInput);
        } else {
            // selects a picture for the accepted quote
            if (botAttributes.getCurrentState().equals(BotState.AWAITING_IMAGE_CHOICE)) {
                log.debug("current state is AWAITING_IMAGE_CHOICE");
                handleImageChoiceResponse(chatId, userInput);
            }
            if (botAttributes.getCurrentState().equals(BotState.AWAITING_PUBLISHING)) {
                log.debug("current state is AWAITING_PUBLISHING");
                handlePublishingChoiceResponse(chatId, userInput);
            }
            if (botAttributes.getCurrentState().equals(BotState.POSTPONE)) {
                log.debug("current state is POSTPONE");
                handlePostponeChoiceResponse(chatId, userInput);
            }
            // Handle user's response to username input (registration new user)
            if (botAttributes.getCurrentState().equals(BotState.AWAITING_USERNAME_INPUT)) {
                handleUsernameInputResponse(update);
            }
            if (botAttributes.getCurrentState().equals(BotState.AWAITING_PASSWORD_INPUT)) {
                handlePasswordInputResponse(update);
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
        log.debug("handleCallbackQuery push in lastCallbackMessage: {}", botAttributes.getLastCallbackMessage());
        Long chatId = callbackQuery.getMessage().getChatId();
        log.debug("chatID = {}", chatId);

        String[] dataParts = callbackQuery.getData().split("-");
        String action = dataParts[0];
        Long quoteId = Long.valueOf(dataParts[1]);
        log.debug("action = {}  quoteId = {}", action, quoteId);

        if (botAttributes.getCurrentState().equals(BotState.FREE_STATE)) {
            switch (action) {
                case "edit" -> editQuote(chatId, quoteId);
                case "confirm" -> confirmQuote(chatId, quoteId);
                case "reject" -> rejectQuote(chatId, quoteId);
                default -> log.warn("no action");
            }
        }
    }

    public QuoteDTO publishQuoteToGroup(QuoteDTO quoteDTO) {
//        if (!botAttributes.getConfirmedContent().isEmpty()) {
        if (!quoteDTO.getContent().isEmpty()) {
            telegramBot.sendMessage(groupChatId, null, botAttributes.getConfirmedContent());
        }
        if (!botAttributes.getConfirmedUrl().isEmpty()) {
            telegramBot.sendImageToChat(groupChatId, botAttributes.getConfirmedUrl());
            quoteDTO.setImageUrl(botAttributes.getConfirmedUrl());
        }
        return quoteDTO;
    }

    public InlineKeyboardMarkup getEditKeyboardMarkup(Long quoteId) {
        return quoteKeyboardService.getEditKeyboardMarkup(quoteId);
    }

    private void handleImageChoiceResponse(Long chatId, String userInput) {
        log.debug("handleImageChoiceResponse");
        String chosenImageUrl = null;

        if (isValidImageChoice(userInput, botAttributes.getImageUrls().size())) {
            if (!userInput.equals("0")) {
                chosenImageUrl = botAttributes.getImageUrls().get(Integer.parseInt(userInput) - 1);
                botAttributes.setConfirmedUrl(chosenImageUrl);
                log.debug("userInput = {}, chosenImageUrl = {}", userInput, chosenImageUrl);
            }
        } else {
            log.warn("NumberFormatException in NumberFormatException with choice = {}", userInput);
            telegramBot.sendMessage(chatId, null, "Выбери изображение от 0 до 10 (0 - пост без изображений).");
        }
        postingQuote(chatId);
    }

    private void postingQuote(Long chatId) {
        log.debug("in postingQuote with lastCallbackMessage: {}, chosenImageUrl: {}",
                botAttributes.getLastCallbackMessage(), botAttributes.getConfirmedUrl());
        // choose whether to publish the quote immediately or postpone it
        log.debug("in postingQuote currentState = " + botAttributes.getCurrentState());
        telegramBot.sendMessage(chatId, null, "Публиковать [сразу] или [отложить]?");
        botAttributes.setCurrentState(BotState.AWAITING_PUBLISHING);
    }

    private void handlePublishingChoiceResponse(Long chatId, String userInput) {
        log.debug("in handlePublishingChoiceResponse currentState = {}, userInput = {}", botAttributes.getCurrentState(), userInput);

        switch (userInput) {
            case "сразу":
                // publishing a post in a telegram group
                QuoteDTO quoteDTO = publishQuoteToGroup(new QuoteDTO());
                // send the quote to be saved in the database
                quoteDTO.setId(botAttributes.getQuoteId());
                quoteDTO.setContent(botAttributes.getConfirmedContent());
                quoteDTO.setStatus(QuoteStatus.PUBLISHED);
                log.debug("send the quote to be saved in the database");
                String confirmUrl = API_BASE_URL + "/quotes/confirm";
                apiService.sendRequestAndHandleResponse(confirmUrl, HttpMethod.POST, quoteDTO, "пост опубликован сразу", null);
                break;
            case "отложить":
                log.debug("case 'отложить'");
                telegramBot.sendMessage(chatId, null, "Напиши дату публикации в виде: [yyyy-MM-dd HH:mm:ss] или напиши [случайно]");
                botAttributes.setCurrentState(BotState.POSTPONE);
                break;
            default:
                log.debug("case default, userInput in handlePublishingChoiceResponse = {}", userInput);
                if (!isValidImageChoice(userInput, botAttributes.getImageUrls().size())) {
                    telegramBot.sendMessage(chatId, null, "Напиши выбор в виде: [сразу] или [отложить]");
                }
                break;
        }
    }

    private void handlePostponeChoiceResponse(Long chatId, String userInput) {
        if (userInput.equalsIgnoreCase("случайно")) {

            botAttributes.setCurrentState(BotState.FREE_STATE);
        } else {
            // Parse the input as a date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date pendingTime = dateFormat.parse(userInput);
                // send the quote to be saved in the database
                postponePublishing(chatId, pendingTime);
            } catch (ParseException e) {
                // Handle invalid date format error
                if (!userInput.equals("отложить")) {
                    telegramBot.sendMessage(chatId, null, "Неверный формат. Напиши в формате: [yyyy-MM-dd HH:mm:ss] или: [случайно].");
                }
            }
        }
    }

    private void postponePublishing(Long chatId, Date pendingTime) {
        QuoteDTO quoteDTO = new QuoteDTO();
        quoteDTO.setId(botAttributes.getQuoteId());
        quoteDTO.setContent(botAttributes.getConfirmedContent());
        quoteDTO.setPendingTime(pendingTime);
        quoteDTO.setStatus(QuoteStatus.PENDING);
        quoteDTO.setImageUrl(botAttributes.getConfirmedUrl());

        String confirmUrl = API_BASE_URL + "/quotes/pending";
        apiService.sendRequestAndHandleResponse(confirmUrl, HttpMethod.POST, quoteDTO, "успешно отложена на " + pendingTime, null);

        clearBotAttributes();
    }

    private void editQuote(Long chatId, Long quoteId) {
        log.debug("editQuote with chatId: {} and quoteId: {}", chatId, quoteId);
        // Edit the original message to remove the inline keyboard
        telegramBot.removeKeyboard(chatId);

        InlineKeyboardMarkup keyboard = quoteKeyboardService.getEditKeyboardMarkup(quoteId);
        String editedText = (!quoteText.isEmpty()) ? quoteText.peek() : "write quoteText starting with q:";
        log.debug("editedText = {}", editedText);
        telegramBot.sendMessage(chatId, keyboard, editedText);
    }

    private void confirmQuote(Long chatId, Long quoteId) {
        log.debug("confirmQuote with chatId: {} and quoteId: {} and quoteText.isEmpty: {}", chatId, quoteId, quoteText.isEmpty());
        String content = (!quoteText.isEmpty()) ? quoteText.pop() : botAttributes.getLastCallbackMessage();
        botAttributes.setConfirmedContent(content);
        // searching images for content with Google custom search api
        log.debug("content = {}", content);
        // limit the length of the get request
        String contentRequest = content.substring(0, Math.min(content.length(), 1024));
        botAttributes.setImageUrls(googleCustomSearchService.searchImagesByKeywords(contentRequest));
        log.debug("size of botAttributes.setImageUrls = {}", botAttributes.getImageUrls().size());
        botAttributes.setQuoteId(Long.valueOf(quoteId));
        // sending selectable images to the user
        sendImagesToChoice(chatId);
    }

    private void rejectQuote(Long chatId, Long quoteId) {
        log.debug("rejectQuote with chatId: {} and quoteId: {}", chatId, quoteId);
        String rejectUrl = API_BASE_URL + "/quotes/reject?id=" + quoteId;
        log.debug("rejectUrl quote with url = {}", rejectUrl);

        apiService.sendRequestAndHandleResponse(rejectUrl, HttpMethod.DELETE, null, "успешно удалена", null);
    }

    private void sendImagesToChoice(Long chatId) {
        for (int i = 0; i < IMAGE_COUNT_TO_CHOICE; i++) {
            log.debug("in sendImagesToChoice botAttributes.getImageUrls().size() = {}", botAttributes.getImageUrls().size());
            if (botAttributes.getImageUrls().size() > i) {
                String imageUrl = botAttributes.getImageUrls().get(i);
                byte[] imageBytes = downloadImage(imageUrl);
                // Send the image as an attachment
                telegramBot.sendImageAttachment(chatId, imageBytes, i + 1);
            }
        }
        // ask for image choice
        telegramBot.removeKeyboard(chatId);
        telegramBot.sendMessage(chatId, null, "Выбери изображение, введя цифру в пределах [1-" + botAttributes.getImageUrls().size() + "]");
        botAttributes.setCurrentState(BotState.AWAITING_IMAGE_CHOICE);
    }

    private byte[] downloadImage(String imageUrl) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.exchange(imageUrl, HttpMethod.GET, null, byte[].class);
        return response.getBody();
    }

    private boolean isValidImageChoice(String userInput, int countImages) {
        try {
            int choice = Integer.parseInt(userInput);
            return choice >= 0 && choice <= countImages;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void handleUsernameInputResponse(Update update) {
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
        } else {
            log.warn("username is already taken");
            telegramBot.sendMessage(chatId, null, "Имя пользователя уже занято. Пожалуйста, выберите другое имя.");
        }
    }

    private void handlePasswordInputResponse(Update update) {
        log.debug("QuoteService in handlePasswordInputResponse");
        String password = update.getMessage().getText().trim();
        Long chatId = update.getMessage().getChatId();
        User user = update.getMessage().getFrom();
        Long usertgId = user.getId();
        // Forward the response to the UserRegistrationService
        UserDTO userDTO = userRegistrationService.handlePasswordInput(usertgId, chatId, password);
        if (userDTO != null) {
            apiService.registerUser(userDTO);
            // Remove the user from the registration in progress list
            userRegistrationService.completeRegistration(usertgId, chatId);
        } else {
            log.warn("userRegistrationService.handlePasswordInput return null in userDTO ");
        }
    }

    private void clearBotAttributes() {
        botAttributes.setCurrentState(BotState.FREE_STATE);
        botAttributes.setConfirmedUrl("");
        botAttributes.setConfirmedContent("");
        botAttributes.setImageUrls(new ArrayList<>());
        log.debug("botAttributes.setImageUrls cleared");
    }

}
