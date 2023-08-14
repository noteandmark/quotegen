package com.andmark.quotebot.service;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.service.keyboard.QuoteKeyboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Arrays;
import java.util.List;

import static com.andmark.quotebot.config.BotConfig.adminChatId;
import static com.andmark.quotebot.config.BotConfig.botToken;

@Service
@Slf4j
public class ApiServiceImpl implements ApiService{

    private final Bot telegramBot;
    private final BotAttributes botAttributes;
    private final RestTemplate restTemplate;
    private final QuoteKeyboardService quoteKeyboardService;

    public ApiServiceImpl(@Lazy Bot telegramBot, BotAttributes botAttributes, RestTemplate restTemplate, QuoteKeyboardService quoteKeyboardService) {
        this.telegramBot = telegramBot;
        this.botAttributes = botAttributes;
        this.restTemplate = restTemplate;
        this.quoteKeyboardService = quoteKeyboardService;
    }

    @Override
    public QuoteDTO getNextQuote() {
        String quoteUrlGetNext = BotConfig.API_BASE_URL + "/quotes/get-next";
        ResponseEntity<QuoteDTO> response = restTemplate.getForEntity(quoteUrlGetNext, QuoteDTO.class);

        QuoteDTO quoteDTO = response.getBody();
        log.info("get quote with id: {}", quoteDTO.getId());
        telegramBot.sendMessage(adminChatId, quoteKeyboardService.getEditKeyboardMarkup(String.valueOf(quoteDTO.getId())), formatQuoteText(quoteDTO));

        return quoteDTO;
    }

    @Override
    public List<QuoteDTO> getPendingQuotes() {
        ResponseEntity<QuoteDTO[]> response = restTemplate.getForEntity(BotConfig.API_BASE_URL + "/quotes/get-pending", QuoteDTO[].class);
        log.debug("get response from restTemplate.getForEntity");
        return Arrays.asList(response.getBody());
    }

    public void sendRequestAndHandleResponse(String url, HttpMethod httpMethod, Object requestBody, String successMessage, InlineKeyboardMarkup keyboard) {
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
                if (keyboard != null) {
                    telegramBot.removeKeyboard(adminChatId);
                }
                telegramBot.sendMessage(adminChatId, keyboard, successMessage);
            } else {
                log.error("Failed to send request. Status code: {}", response.getStatusCode());
            }
        } catch (HttpClientErrorException ex) {
            log.warn("HttpClientErrorException while sending request: {}", ex.getMessage());
            telegramBot.sendMessage(adminChatId, null, "HttpClientErrorException");
        }
    }

    private String formatQuoteText(QuoteDTO quoteDTO) {
        return quoteDTO.getContent() + "\n\n"
                + quoteDTO.getBookAuthor() + "\n"
                + quoteDTO.getBookTitle();
    }

}
