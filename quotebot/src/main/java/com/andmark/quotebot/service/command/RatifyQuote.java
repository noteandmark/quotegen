package com.andmark.quotebot.service.command;

import com.andmark.quotebot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.springframework.http.HttpHeaders;

@Component
@Slf4j
public class RatifyQuote {
    public static final String API_BASE_URL = BotConfig.API_BASE_URL;
    public static final String botToken = BotConfig.botToken;
    private final RestTemplate restTemplate;

    @Autowired
    public RatifyQuote(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void handleCallbackQuery(CallbackQuery callbackQuery) {
        log.debug("in handleCallbackQuery");
        String callbackData = callbackQuery.getData();
        String[] dataParts = callbackData.split("-");
        if (dataParts.length != 2) {
            log.warn("Invalid callback data format: {}", callbackData);
            return;
        }

        String action = dataParts[0];
        Long quoteId = Long.parseLong(dataParts[1]);
        log.debug("action = {}", action);
        log.debug("quoteId = {}", quoteId);

        if ("accept".equals(action)) {
            confirmQuote(quoteId);
        } else if ("reject".equals(action)) {
            rejectQuote(quoteId);
        } else {
            log.warn("Unknown action in callback data: {}", callbackData);
        }
    }

    private void confirmQuote(Long quoteId) {
        String confirmUrl = API_BASE_URL + "/quotes/confirm?id=" + quoteId;
        log.debug("ratify quote confirm with confirmUrl = {}", confirmUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + botToken); //TODO make encryption
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Void> response = restTemplate.exchange(confirmUrl, HttpMethod.POST, requestEntity, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Quote with id {} has been confirmed.", quoteId);
        } else {
            log.error("Failed to confirm quote with id {}: {}", quoteId, response.getStatusCode());
        }
    }

    private void rejectQuote(Long quoteId) {
        String rejectUrl = BotConfig.API_BASE_URL + "/quotes/reject?id=" + quoteId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + botToken);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Void> response = restTemplate.exchange(rejectUrl, HttpMethod.DELETE, requestEntity, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Quote with id {} has been rejected.", quoteId);
        } else {
            log.error("Failed to reject quote with id {}: {}", quoteId, response.getStatusCode());
        }
    }

}
