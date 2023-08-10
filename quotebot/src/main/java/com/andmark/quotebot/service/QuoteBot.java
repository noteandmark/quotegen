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
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class QuoteBot extends TelegramLongPollingCommandBot {
    //    private final RatifyQuote ratifyQuote;
    public static final String API_BASE_URL = BotConfig.API_BASE_URL;
    public static final String botToken = BotConfig.botToken;

    private final RestTemplate restTemplate;

    public QuoteBot(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @Override
    public void processNonCommandUpdate(Update update) {
        SendMessage message;
        if (update.hasMessage()) {
            Thread messageThread = new Thread(() -> handleIncomingMessage(update.getMessage()));
            messageThread.start();
        } else if (update.hasCallbackQuery()) {
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
            response.setText("Hi!");

            execute(response);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Handle callback queries
    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        log.debug("update.hasCallbackQuery() = {}", callbackQuery.getMessage().toString());
        String str = callbackQuery.getData().toString();
        String action = str.substring(0, str.indexOf("_"));
        log.debug("action = {}", action);

        switch (action) {
            case "decision":
                decisionQuote(callbackQuery);
                break;
            default:
                log.warn("no action");
                break;
        }

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
        ResponseEntity<Void> response = restTemplate.exchange(decisionUrl, httpMethod , requestEntity, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("quote with id {} has been {}.", quoteId, action);
            String chatId = callbackQuery.getMessage().getChatId().toString();
            SendMessage answer = new SendMessage();
            answer.setChatId(chatId);
            answer.setText("Цитата с id = " + quoteId + " " + action + "\n");
            try {
                execute(answer);
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
