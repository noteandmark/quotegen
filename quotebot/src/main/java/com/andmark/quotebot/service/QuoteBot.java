package com.andmark.quotebot.service;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.service.command.RequestQuoteCommand;
import com.andmark.quotebot.service.command.StartCommand;
import com.andmark.quotebot.service.enums.BotState;
import com.andmark.quotebot.service.googleapi.GoogleCustomSearchService;
import com.andmark.quotebot.service.keyboard.InlineButton;
import com.andmark.quotebot.service.keyboard.InlineKeyboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class QuoteBot extends TelegramLongPollingCommandBot {
    public static final String API_BASE_URL = BotConfig.API_BASE_URL;
    public static final String botToken = BotConfig.botToken;
    private static final int IMAGE_COUNT_TO_CHOICE = 10;

    private Stack<String> quoteText;
//    private BotState currentState;

    private final BotAttributes botAttributes;
    private final RestTemplate restTemplate;
    private final InlineKeyboardService inlineKeyboardService;
    private final GoogleCustomSearchService googleCustomSearchService;
    private final QuoteApiService quoteApiService;

    public QuoteBot(BotAttributes botAttributes, RestTemplate restTemplate, InlineKeyboardService inlineKeyboardService, GoogleCustomSearchService googleCustomSearchService, QuoteApiService quoteApiService) {
        this.botAttributes = botAttributes;
        this.inlineKeyboardService = inlineKeyboardService;
        this.googleCustomSearchService = googleCustomSearchService;
        this.restTemplate = restTemplate;
        this.quoteApiService = quoteApiService;
        //a cache to store edited text
        quoteText = new Stack<>();
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Thread messageThread = new Thread(() -> handleIncomingMessage(update.getMessage()));
            messageThread.start();
        } else if (update.hasCallbackQuery()) {
            Thread callbackThread = new Thread(() -> handleCallbackQuery(update.getCallbackQuery()));
            callbackThread.start();
        }
    }

    // Handle incoming messages
    private void handleIncomingMessage(Message message) {
        log.debug("handleIncomingMessage in chatId: {}, messageId: {}", message.getChatId(), message.getMessageId());
        String userInput = message.getText().trim();
        Long chatId = message.getChatId();

        log.debug("text = {}", userInput);
        if (message.getText().startsWith("q:")) {
            quoteText.push(userInput.substring(2));
            log.debug("quoteText offer text = {}", userInput);
        }
        // other cases
        else {
            // selects a picture for the accepted quote
//            if (currentState == BotState.AWAITING_IMAGE_CHOICE) {
            if (botAttributes.getCurrentState().equals(BotState.AWAITING_IMAGE_CHOICE)) {
                log.debug("current state is AWAITING_IMAGE_CHOICE");
                // TODO: возможно, все эти методы выбора можно объединить с выбором параметра
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


    private void handleImageChoiceResponse(Long chatId, String userInput) {
        log.debug("handleImageChoiceResponse");
        Integer chosenImageNumber = -1;
        String chosenImageUrl = null;
        try {
            chosenImageNumber = Integer.parseInt(userInput);
            log.debug("chosenImageNumber = {}", chosenImageNumber);
        } catch (NumberFormatException e) {
            log.warn("NumberFormatException in NumberFormatException with choice = {}", userInput);
            sendMessage(chatId, null, "Выбери изображение от 0 до 10 (0 - пост без изображений)");
        }
        int countTimer = 0;
        while (botAttributes.getCurrentState().equals(BotState.AWAITING_IMAGE_CHOICE)) {
            log.debug("currentState = " + botAttributes.getCurrentState());
            switch (chosenImageNumber) {
                case 0:
                    botAttributes.setCurrentState(BotState.FREE_STATE);
                    log.debug("case 0");
                    break;
                case 1,2,3,4,5,6,7,8,9,10:
                    chosenImageUrl = botAttributes.getImageUrls().get(chosenImageNumber - 1);
                    botAttributes.setConfirmedUrl(chosenImageUrl);
                    botAttributes.setCurrentState(BotState.FREE_STATE);
                    log.debug("case 1-10, chosenImageUrl = {}", chosenImageUrl);
                    break;
                default:
                    log.debug("default case");
                    if (countTimer < 10) {
                        sendMessage(chatId, null, "Выбери изображение от 0 до 10 (0 - пост без изображений)");
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    } else {
                        botAttributes.setCurrentState(BotState.FREE_STATE);
                        throw new RuntimeException();
                    }
            }
            countTimer++;
        }
        postingQuote(chatId);

        botAttributes.setImageUrls(new ArrayList<>());
//        log.debug("currentState = {}, botAttributes.setImageUrls cleared", currentState);
        log.debug("currentState = {}, botAttributes.setImageUrls cleared", botAttributes.getCurrentState());
    }

    private void postingQuote(Long chatId) {
        log.debug("in postingQuote with lastCallbackMessage: {}, chosenImageUrl: {}",
                botAttributes.getLastCallbackMessage(), botAttributes.getConfirmedUrl());
        // choose whether to publish the quote immediately or postpone it
        botAttributes.setCurrentState(BotState.AWAITING_PUBLISHING);
        sendMessage(chatId, null, "Публиковать [сразу] или [отложить]?");

//        List<QuoteDTO> pendingQuotes = quoteApiService.getPendingQuotes();
//        log.debug("get pendingQuotes, count = {}", pendingQuotes.size());

        //        botAttributes.setConfirmedContent("");
    }

    private void handlePublishingChoiceResponse(Long chatId, String userInput) {
        log.debug("handlePublishingChoiceResponse");
        switch (userInput) {
            case "сразу":
                // TODO: реализировать публикацию сразу в группу
                // here is code
                // !!!!!!!!!!!!
                botAttributes.setCurrentState(BotState.FREE_STATE);
                break;
            case "отложить":
                sendMessage(chatId, null, "Напиши дату публикации в виде: [yyyy-MM-dd HH:mm:ss] или напиши [случайно]");
                botAttributes.setCurrentState(BotState.POSTPONE);
                postponePublishing(chatId);
                break;
            default:
                sendMessage(chatId, null, "Напиши выбор в виде: [сразу] или [отложить]");
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

//                quoteApiService.sendDeferredQuote(quoteDTO, deferredDate);

                // send the quote to be saved in the database
                QuoteDTO quoteDTO = new QuoteDTO();
                quoteDTO.setId(botAttributes.getQuoteId());
                quoteDTO.setContent(botAttributes.getConfirmedContent());
                quoteDTO.setPendingTime(pendingTime);

                String confirmUrl = API_BASE_URL + "/quotes/pending";
                sendRequestAndHandleResponse(confirmUrl, HttpMethod.POST, quoteDTO, "успешно отложена на " + pendingTime, chatId, botAttributes.getQuoteId());

                botAttributes.setCurrentState(BotState.FREE_STATE);
            } catch (ParseException e) {
                // Handle invalid date format error
                sendMessage(chatId, null, "Неверный формат. Напиши в формате: [yyyy-MM-dd HH:mm:ss] или: [случайно].");
            }
        }
    }

    private void postponePublishing(Long chatId) {

    }



    // Handle callback queries
    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        log.debug("callbackQuery.getMessage().getText() = {}", callbackQuery.getMessage().getText());
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

        switch (action) {
            case "edit" -> editQuote(chatId, quoteId);
            case "confirm" -> confirmQuote(chatId, quoteId);
            case "reject" -> rejectQuote(chatId, quoteId);
            default -> log.warn("no action");
        }
    }

    private void editQuote(Long chatId, Long quoteId) {
        log.debug("editQuote with chatId: {} and quoteId: {}", chatId, quoteId);
        // Edit the original message to remove the inline keyboard
        removeKeyboard(chatId);

        InlineKeyboardMarkup keyboard = getEditKeyboardMarkup(quoteId);
        String editedText = (!quoteText.isEmpty()) ? quoteText.peek() : "write quoteText starting with q:";
        log.debug("editedText = {}", editedText);
        sendMessage(chatId, keyboard, editedText);
    }

    private void confirmQuote(Long chatId, Long quoteId) {
        log.debug("confirmQuote with chatId: {} and quoteId: {} and quoteText.isEmpty: {}", chatId, quoteId, quoteText.isEmpty());
        String content = (!quoteText.isEmpty()) ? quoteText.peek() : botAttributes.getLastCallbackMessage();
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

        // send the quote to be saved in the database
        QuoteDTO quoteDTO = new QuoteDTO();
        quoteDTO.setId(quoteId);
        quoteDTO.setContent(content);
        String confirmUrl = API_BASE_URL + "/quotes/confirm";
        // !!! wait for next string !!!
        sendRequestAndHandleResponse(confirmUrl, HttpMethod.POST, quoteDTO, "успешно принята", chatId, quoteId);
        /// !!! don't delete
    }

    private void sendImagesToChoice(Long chatId) {
        for (int i = 0; i < IMAGE_COUNT_TO_CHOICE; i++) {
            log.debug("in sendImagesToChoice botAttributes.getImageUrls().size() = {}", botAttributes.getImageUrls().size());
            if (botAttributes.getImageUrls().size() > i) {
                String imageUrl = botAttributes.getImageUrls().get(i);
                byte[] imageBytes = downloadImage(imageUrl);
                // Send the image as an attachment
                sendImageAttachment(chatId, imageBytes, i + 1);
            }
        }
        // ask for image choice
        sendMessage(chatId, null, "Please choose an image by typing the corresponding number (1-10):");
        botAttributes.setCurrentState(BotState.AWAITING_IMAGE_CHOICE);
    }

    private byte[] downloadImage(String imageUrl) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.exchange(imageUrl, HttpMethod.GET, null, byte[].class);
        return response.getBody();
    }

    private void sendImageAttachment(Long chatId, byte[] imageBytes, int imageNumber) {
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

    private void rejectQuote(Long chatId, Long quoteId) {
        log.debug("rejectQuote with chatId: {} and quoteId: {}", chatId, quoteId);
        String rejectUrl = API_BASE_URL + "/quotes/reject?id=" + quoteId;
        log.debug("rejectUrl quote with url = {}", rejectUrl);

        sendRequestAndHandleResponse(rejectUrl, HttpMethod.DELETE, null, "успешно удалена", chatId, quoteId);
    }

    // TODO : вынести этот метод в QuoteApiService
    private void sendRequestAndHandleResponse(String url, HttpMethod httpMethod, Object requestBody, String successMessage, Long chatId, Long quoteId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + botToken);

        HttpEntity<?> requestEntity = (requestBody != null) ? new HttpEntity<>(requestBody, headers) : new HttpEntity<>(headers);
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    httpMethod,
                    requestEntity,
                    Void.class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info(successMessage);
                removeKeyboard(chatId);
                sendMessage(chatId, null, "Цитата с id = " + quoteId + " " + successMessage);
            } else {
                log.error("Failed to send request. Status code: {}", response.getStatusCode());
            }
        } catch (HttpClientErrorException ex) {
            log.warn("HttpClientErrorException while sending request: {}", ex.getMessage());
            sendMessage(chatId, null, "HttpClientErrorException");
        }
    }

    private void sendMessage(Long chatId, InlineKeyboardMarkup keyboard, String textToSend) {
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

    private void removeKeyboard(Long chatId) {
        EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(botAttributes.getLastMessageId());
        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            log.error("Can't remove the inline keyboard in messageId = {}", botAttributes.getLastMessageId());
        }
    }

    private InlineKeyboardMarkup getEditKeyboardMarkup(Long quoteId) {
        List<InlineButton> buttons = new ArrayList<>();
        buttons.add(new InlineButton("Edit", "edit-" + quoteId));
        buttons.add(new InlineButton("Accept", "confirm-" + quoteId));
        buttons.add(new InlineButton("Reject", "reject-" + quoteId));

        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardService.createInlineKeyboard(buttons);
        return keyboardMarkup;
    }

    @Override
    public void onRegister() {
        register(new StartCommand());
        register(new RequestQuoteCommand(inlineKeyboardService));
    }

    @Override
    public String getBotUsername() {
        return BotConfig.botUsername;
    }

    public String getBotToken() {
        return BotConfig.botToken;
    }

}