package com.andmark.quotebot.service;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.service.command.RequestQuoteCommand;
import com.andmark.quotebot.service.command.StartCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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

    // a cache to store last messages sent by the administrator
    private final Queue<SendMessage> lastMessagesQueue = new LinkedList<>();
    // a map to store the last admin message text along with its chat ID
    private final Map<Long, String> lastAdminMessages = new HashMap<>();

    private final RestTemplate restTemplate;

    public QuoteBot(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        SendMessage message;
        if (update.hasMessage() && update.getMessage().hasText()) {
            System.out.println("hasMessage: id = " + update.getMessage().getMessageId());
            Thread messageThread = new Thread(() -> handleIncomingMessage(update.getMessage()));
            messageThread.start();
        } else if (update.hasCallbackQuery()) {
            System.out.println("hasCallbackQuery: id = " + update.getCallbackQuery().getMessage().getMessageId());
            Thread callbackThread = new Thread(() -> handleCallbackQuery(update.getCallbackQuery()));
            callbackThread.start();
        }
    }

    // Handle incoming messages
    private void handleIncomingMessage(Message message) {
        try {

            String chatId = message.getChatId().toString();
            SendMessage response = new SendMessage();
            response.setChatId(chatId);
            lastMessagesQueue.offer(response);


            // Limit the queue size
            int maxQueueSize = 10; // Choose an appropriate size
            while (lastMessagesQueue.size() > maxQueueSize) {
                lastMessagesQueue.poll(); // Remove the oldest item
            }


            String text;
            if (message.getText().startsWith("q:")) {
                text = message.getText().substring(2);
                lastAdminMessages.put(message.getChatId(), text);
            } else {
                text = "write /help to read commands";
            }

//            if (message.isUserMessage() && message.getFrom().getUserName().equals("and_mark")) {
//                System.out.println("message.getText():");
//                System.out.println("message.getText() = " + message.getText());
//            }

            response.setText(text);

            execute(response);
        } catch (TelegramApiException e) {
            log.warn("TelegramApiException in handleIncomingMessage");
        }
    }

    // Handle callback queries
    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        log.debug("update.hasCallbackQuery() = {}", callbackQuery.getMessage().toString());
        String str = callbackQuery.getData();
        String action = str.substring(0, str.indexOf("_"));
        log.debug("action = {}", action);
        System.out.println("action = " + action);

        switch (action) {
            case "decision":
                decisionQuote(callbackQuery);
                break;
            case "edit":
                System.out.println("case edit");
                editQuote(callbackQuery); //need to implement
                break;
            default:
                log.warn("no action");
                break;
        }

    }

    private void editQuote(CallbackQuery callbackQuery) {
        System.out.println("editQuote message id = " + callbackQuery.getMessage().getMessageId());
        String callbackData = callbackQuery.getData();
        System.out.println("callbackQuery.getData() = " + callbackQuery.getData());
        String[] dataParts = callbackData.split("_");
        String[] dataAction = dataParts[1].split("-");
        if (dataAction.length != 2) {
            log.warn("Invalid callback data format: {}", callbackData);
            return;
        }
        String action = dataAction[0];
        Long quoteId = Long.parseLong(dataAction[1]);

        Long chatId = callbackQuery.getMessage().getChatId();
        String lastAdminMessageText = lastAdminMessages.get(chatId);

        SendMessage lastMessage = lastMessagesQueue.peek();

        if (lastAdminMessageText != null) {
            // Use the last admin message text for the new quote text
            lastMessage.setText(lastAdminMessageText);
            lastMessage.setReplyMarkup(getEditKeyboardMarkup(quoteId));

            try {
                execute(lastMessage);
            } catch (TelegramApiException e) {
                log.error("Failed to send edit message for quote {}", quoteId, e);
            }
        } else {
            System.out.println("error");
            log.error("No last admin message found for editing.");
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
        acceptButton.setCallbackData("decision_confirm-" + quoteId);
        row.add(acceptButton);

        InlineKeyboardButton rejectButton = new InlineKeyboardButton("Reject");
        rejectButton.setCallbackData("decision_reject-" + quoteId);
        row.add(rejectButton);

        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }


    private void decisionQuote(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        String[] dataParts = callbackData.split("_");
        String[] dataAction = dataParts[1].split("-");
        if (dataAction.length != 2) {
            log.warn("Invalid callback data format: {}", callbackData);
            return;
        }
        String action = dataAction[0];
        Long quoteId = Long.parseLong(dataAction[1]);
        log.debug("action = {}, quoteId = {}", action, quoteId);
        String decisionUrl = API_BASE_URL + "/quotes/" + action + "?id=" + quoteId;
        log.debug("decision quote with url = {}", decisionUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + botToken); //TODO make encryption
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        HttpMethod httpMethod = action.equals("reject") ? HttpMethod.DELETE : HttpMethod.POST;
        ResponseEntity<Void> response = restTemplate.exchange(decisionUrl, httpMethod, requestEntity, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("quote with id {} has been {}.", quoteId, action);
            Long chatId = callbackQuery.getMessage().getChatId();
            SendMessage answer = new SendMessage();
            answer.setChatId(chatId);
            answer.setText("Цитата с id = " + quoteId + " " + action + "\n");

            // Edit the original message to remove the inline keyboard
            int messageId = callbackQuery.getMessage().getMessageId();
            EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup();
            editMessage.setChatId(chatId);
            editMessage.setMessageId(messageId);

            try {
                execute(editMessage);
                execute(answer);
                log.debug("execute answer and empty keyboard");
            } catch (TelegramApiException e) {
                log.error("Failed to send action message for quote {}", quoteId, e);
            }
        } else {
            log.error("Failed to action quote with id {}: {}", quoteId, response.getStatusCode());
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
