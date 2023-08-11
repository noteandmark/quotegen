package com.andmark.quotebot.service;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.service.command.RequestQuoteCommand;
import com.andmark.quotebot.service.command.StartCommand;
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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Component
@Slf4j
public class QuoteBot extends TelegramLongPollingCommandBot {
    public static final String API_BASE_URL = BotConfig.API_BASE_URL;
    public static final String botToken = BotConfig.botToken;

    private Stack<String> quoteText;
    private int lastMessageId;
    private String lastCallbackMessage;

    private final RestTemplate restTemplate;

    public QuoteBot(RestTemplate restTemplate) {
        //a cache to store edited text
        quoteText = new Stack<>();
        this.restTemplate = restTemplate;
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
        if (message.getText().startsWith("q:")) {
            String text = message.getText().substring(2);
            quoteText.push(text);
            log.debug("quoteText offer text = {}", text);
        }
    }

    // Handle callback queries
    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        lastMessageId = callbackQuery.getMessage().getMessageId();
        log.debug("handleCallbackQuery with lastMessageId: {}", lastMessageId);
        lastCallbackMessage = callbackQuery.getMessage().getText();
        log.debug("handleCallbackQuery push in lastCallbackMessage: {}", lastCallbackMessage);
        Long chatId = callbackQuery.getMessage().getChatId();
        log.debug("chatID = {}", chatId);

        String callbackData = callbackQuery.getData();
        String[] dataParts = callbackData.split("-");
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
        log.debug("confirmQuote with chatId: {} and quoteId: {}", chatId, quoteId);
        String content = (!quoteText.isEmpty()) ? quoteText.peek() : lastCallbackMessage;
        log.debug("content in confirmQuote = {}", content);

        // Create a QuoteDTO object with id and content
        QuoteDTO quoteDTO = new QuoteDTO();
        quoteDTO.setId(quoteId);
        quoteDTO.setContent(content);

        String confirmUrl = API_BASE_URL + "/quotes/confirm";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + botToken);
        HttpEntity<QuoteDTO> requestEntity = new HttpEntity<>(quoteDTO, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                confirmUrl,
                HttpMethod.POST,
                requestEntity,
                Void.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Quote confirm request sent successfully.");
            removeKeyboard(chatId);
            sendMessage(chatId, null, "Цитата с id = " + quoteId + " успешно принята");
        } else {
            log.error("Failed to send quote confirm request. Status code: {}", response.getStatusCode());
        }
    }

    private void rejectQuote(Long chatId, Long quoteId) {
        log.debug("rejectQuote with chatId: {} and quoteId: {}", chatId, quoteId);
        String rejectUrl = API_BASE_URL + "/quotes/reject?id=" + quoteId;
        log.debug("rejectUrl quote with url = {}", rejectUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + botToken);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Void> response = null;
        try {
            response = restTemplate.exchange(
                    rejectUrl,
                    HttpMethod.DELETE,
                    requestEntity,
                    Void.class
            );
        } catch (HttpClientErrorException ex) {
            log.warn("HttpClientErrorException in confirm quote");
            sendMessage(chatId, null, "HttpClientErrorException");
        }
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Quote reject request sent successfully.");
            removeKeyboard(chatId);
            sendMessage(chatId, null, "Цитата с id = " + quoteId + " успешно удалена");
        } else {
            log.error("Failed to send quote reject request. Status code: {}", response.getStatusCode());
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
        editMessage.setMessageId(lastMessageId);
        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            log.error("Can't remove the inline keyboard in messageId = {}", lastMessageId);
        }
    }

    private InlineKeyboardMarkup getEditKeyboardMarkup(Long quoteId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton editButton = new InlineKeyboardButton("Edit");
        editButton.setCallbackData("edit_-" + quoteId);
        row.add(editButton);

        InlineKeyboardButton acceptButton = new InlineKeyboardButton("Accept");
        acceptButton.setCallbackData("confirm-" + quoteId);
        row.add(acceptButton);

        InlineKeyboardButton rejectButton = new InlineKeyboardButton("Reject");
        rejectButton.setCallbackData("reject-" + quoteId);
        row.add(rejectButton);

        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
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