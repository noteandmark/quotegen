package com.andmark.quotebot.service;

import com.andmark.quotebot.domain.enums.QuoteStatus;
import com.andmark.quotebot.dto.QuoteDTO;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import static com.andmark.quotebot.config.BotConfig.*;

@Service
@Slf4j
public class QuoteServiceImpl implements QuoteService{
    private static final int IMAGE_COUNT_TO_CHOICE = 10;

    private final Bot telegramBot;
    private final BotAttributes botAttributes;
    private final ApiServiceImpl apiServiceImpl;
    private final GoogleCustomSearchService googleCustomSearchService;
    private final QuoteKeyboardService quoteKeyboardService;

    private Stack<String> quoteText;

    public QuoteServiceImpl(@Lazy Bot telegramBot, BotAttributes botAttributes, ApiServiceImpl apiServiceImpl, GoogleCustomSearchService googleCustomSearchService, QuoteKeyboardService quoteKeyboardService) {
        this.telegramBot = telegramBot;
        this.botAttributes = botAttributes;
        this.apiServiceImpl = apiServiceImpl;
        this.googleCustomSearchService = googleCustomSearchService;
        this.quoteKeyboardService = quoteKeyboardService;
        //a cache to store edited text
        quoteText = new Stack<>();
    }

    public void handleIncomingMessage(Message message) {
        log.debug("handleIncomingMessage in chatId: {}, messageId: {}", message.getChatId(), message.getMessageId());
        String userInput = message.getText().trim();
        String chatId = String.valueOf(message.getChatId());

        log.debug("text = {}", userInput);
        if (userInput.equals("сброс")) {
            clearBotAttributes();
            telegramBot.sendMessage(String.valueOf(chatId), null, "Состояние сброшено");
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
        }
    }

    public void handleCallbackQuery(CallbackQuery callbackQuery) {
        log.debug("chatId = {}, callbackQuery text() = {}", callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getText());
        botAttributes.setLastMessageId(callbackQuery.getMessage().getMessageId());
        log.debug("handleCallbackQuery with lastMessageId: {}", botAttributes.getLastMessageId());
        botAttributes.setLastCallbackMessage(callbackQuery.getMessage().getText());
        log.debug("handleCallbackQuery push in lastCallbackMessage: {}", botAttributes.getLastCallbackMessage());
        String chatId = String.valueOf(callbackQuery.getMessage().getChatId());
        log.debug("chatID = {}", chatId);

        String[] dataParts = callbackQuery.getData().split("-");
        String action = dataParts[0];
        String quoteId =dataParts[1];
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
        if (!botAttributes.getConfirmedContent().isEmpty()) {
            telegramBot.sendMessage(groupChatId, null, botAttributes.getConfirmedContent());
        }
        if (!botAttributes.getConfirmedUrl().isEmpty()) {
            telegramBot.sendImageToChat(groupChatId, botAttributes.getConfirmedUrl());
            quoteDTO.setImageUrl(botAttributes.getConfirmedUrl());
        }
        return quoteDTO;
    }

    public InlineKeyboardMarkup getEditKeyboardMarkup(String quoteId) {
        return quoteKeyboardService.getEditKeyboardMarkup(quoteId);
    }

    private void handleImageChoiceResponse(String chatId, String userInput) {
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

    private void postingQuote(String chatId) {
        log.debug("in postingQuote with lastCallbackMessage: {}, chosenImageUrl: {}",
                botAttributes.getLastCallbackMessage(), botAttributes.getConfirmedUrl());
        // choose whether to publish the quote immediately or postpone it
        log.debug("in postingQuote currentState = " + botAttributes.getCurrentState());
        telegramBot.sendMessage(chatId, null, "Публиковать [сразу] или [отложить]?");
        botAttributes.setCurrentState(BotState.AWAITING_PUBLISHING);
    }

    private void handlePublishingChoiceResponse(String chatId, String userInput) {
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
                apiServiceImpl.sendRequestAndHandleResponse(confirmUrl, HttpMethod.POST, quoteDTO, "пост опубликован сразу", null);
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

    private void handlePostponeChoiceResponse(String chatId, String userInput) {
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

    private void postponePublishing(String chatId, Date pendingTime) {
        QuoteDTO quoteDTO = new QuoteDTO();
        quoteDTO.setId(botAttributes.getQuoteId());
        quoteDTO.setContent(botAttributes.getConfirmedContent());
        quoteDTO.setPendingTime(pendingTime);
        quoteDTO.setStatus(QuoteStatus.PENDING);
        quoteDTO.setImageUrl(botAttributes.getConfirmedUrl());

        String confirmUrl = API_BASE_URL + "/quotes/pending";
        apiServiceImpl.sendRequestAndHandleResponse(confirmUrl, HttpMethod.POST, quoteDTO, "успешно отложена на " + pendingTime, null);

        clearBotAttributes();
    }

    private void editQuote(String chatId, String quoteId) {
        log.debug("editQuote with chatId: {} and quoteId: {}", chatId, quoteId);
        // Edit the original message to remove the inline keyboard
        telegramBot.removeKeyboard(chatId);

        InlineKeyboardMarkup keyboard = quoteKeyboardService.getEditKeyboardMarkup(quoteId);
        String editedText = (!quoteText.isEmpty()) ? quoteText.peek() : "write quoteText starting with q:";
        log.debug("editedText = {}", editedText);
        telegramBot.sendMessage(chatId, keyboard, editedText);
    }

    private void confirmQuote(String chatId, String quoteId) {
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

    private void rejectQuote(String chatId, String quoteId) {
        log.debug("rejectQuote with chatId: {} and quoteId: {}", chatId, quoteId);
        String rejectUrl = API_BASE_URL + "/quotes/reject?id=" + quoteId;
        log.debug("rejectUrl quote with url = {}", rejectUrl);

        apiServiceImpl.sendRequestAndHandleResponse(rejectUrl, HttpMethod.DELETE, null, "успешно удалена", null);
    }

    private void sendImagesToChoice(String chatId) {
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

    private void clearBotAttributes() {
        botAttributes.setCurrentState(BotState.FREE_STATE);
        botAttributes.setConfirmedUrl("");
        botAttributes.setConfirmedContent("");
        botAttributes.setImageUrls(new ArrayList<>());
        log.debug("botAttributes.setImageUrls cleared");
    }

}
