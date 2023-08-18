package com.andmark.quotebot.service.impl;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.domain.RequestConfiguration;
import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.dto.StatsDTO;
import com.andmark.quotebot.dto.UserDTO;
import com.andmark.quotebot.dto.YesNoApiResponse;
import com.andmark.quotebot.service.ApiService;
import com.andmark.quotebot.service.Bot;
import com.andmark.quotebot.service.BotAttributes;
import com.andmark.quotebot.service.keyboard.QuoteKeyboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.andmark.quotebot.config.BotConfig.adminChatId;
import static com.andmark.quotebot.config.BotConfig.botToken;

@Service
@Slf4j
public class ApiServiceImpl implements ApiService {

    private final Bot telegramBot;
    private final BotAttributes botAttributes;
    private final RestTemplate restTemplate;
    private final QuoteKeyboardService quoteKeyboardService;
    private final BCryptPasswordEncoder passwordEncoder;

    public ApiServiceImpl(@Lazy Bot telegramBot, BotAttributes botAttributes, RestTemplate restTemplate, QuoteKeyboardService quoteKeyboardService, BCryptPasswordEncoder passwordEncoder) {
        this.telegramBot = telegramBot;
        this.botAttributes = botAttributes;
        this.restTemplate = restTemplate;
        this.quoteKeyboardService = quoteKeyboardService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public QuoteDTO getNextQuote() {
        log.debug("api service request: getting next quote");
        String quoteUrlGetNext = BotConfig.API_BASE_URL + "/quotes/get-next";
        ResponseEntity<QuoteDTO> response = restTemplate.getForEntity(quoteUrlGetNext, QuoteDTO.class);

        QuoteDTO quoteDTO = response.getBody();
        log.info("get quote with id: {}", quoteDTO.getId());
        telegramBot.sendMessage(adminChatId, quoteKeyboardService.getEditKeyboardMarkup(quoteDTO.getId()), formatQuoteText(quoteDTO));
        return quoteDTO;
    }

    @Override
    public List<QuoteDTO> getPendingQuotes() {
        log.debug("api service request: getPendingQuotes");
        ResponseEntity<QuoteDTO[]> response = restTemplate.getForEntity(BotConfig.API_BASE_URL + "/quotes/get-pending", QuoteDTO[].class);
        log.debug("get response from restTemplate.getForEntity");
        return Arrays.asList(response.getBody());
    }

    @Override
    public boolean existsByUsertgId(Long id) {
        log.debug("api service request: existsByUsertgId");
        ResponseEntity<Boolean> response = restTemplate.getForEntity(BotConfig.API_BASE_URL + "/users/exists/" + id, Boolean.class);
        return response.getBody();
    }

    @Override
    public boolean existsByUsername(String username) {
        log.debug("api service request: existsByUsername");
        ResponseEntity<Boolean> response = restTemplate.getForEntity(
                BotConfig.API_BASE_URL + "/users/username-taken/" + username, Boolean.class);
        return response.getBody();
    }

    @Override
    public UserRole getUserRole(Long usertgId) {
        log.debug("getting user role for usertgId = {}", usertgId);
        ResponseEntity<UserRole> response = restTemplate.getForEntity(BotConfig.API_BASE_URL + "/users/get-role/" + usertgId, UserRole.class);
        return response.getBody();
    }

    @Override
    public void registerUser(Long chatId, UserDTO userDTO) {
        log.debug("api service request: registerUser with username = {}", userDTO.getUsername());
        // Hash the password before sending it to the API
        String hashedPassword = passwordEncoder.encode(userDTO.getPassword());
        userDTO.setPassword(hashedPassword);
        String registerUserUrl = BotConfig.API_BASE_URL + "/users/register";

        RequestConfiguration requestConfig = new RequestConfiguration.Builder()
                .url(registerUserUrl)
                .httpMethod(HttpMethod.POST)
                .requestBody(userDTO)
                .chatId(chatId)
                .successMessage("Вы получили доступ к боту")
                .keyboard(null)
                .build();
        sendRequestAndHandleResponse(requestConfig);
    }

    @Override
    public void sendRequestAndHandleResponse(RequestConfiguration requestConfig) {
        log.debug("api service sendRequestAndHandleResponse");
        String url = requestConfig.getUrl();
        HttpMethod httpMethod = requestConfig.getHttpMethod();
        Object requestBody = requestConfig.getRequestBody();
        Long chatId = requestConfig.getChatId();
        String successMessage = requestConfig.getSuccessMessage();
        InlineKeyboardMarkup keyboard = requestConfig.getKeyboard();

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
                    telegramBot.removeKeyboard(chatId);
                }
                telegramBot.sendMessage(chatId, keyboard, successMessage);
            } else {
                log.error("Failed to send request. Status code: {}", response.getStatusCode());
            }
        } catch (HttpClientErrorException ex) {
            log.warn("HttpClientErrorException while sending request: {}", ex.getMessage());
            telegramBot.sendMessage(chatId, null, "HttpClientErrorException");
        }
    }

    @Override
    public void deleteUser(Long chatId, Long usertgId) {
        log.debug("delete user with usertgId = {}", usertgId);
        String registerUserUrl = BotConfig.API_BASE_URL + "/users/delete/" + usertgId;

        RequestConfiguration requestConfig = new RequestConfiguration.Builder()
                .url(registerUserUrl)
                .httpMethod(HttpMethod.DELETE)
                .requestBody(null)
                .chatId(chatId)
                .successMessage("Пользователь id = " + "usertgId" + " удален из базы данных бота")
                .keyboard(null)
                .build();
        sendRequestAndHandleResponse(requestConfig);
    }

    @Override
    public String getResponseYesOrNo(String apiUrl) {
        ResponseEntity<YesNoApiResponse> response = restTemplate.exchange(
                apiUrl, HttpMethod.GET, null, YesNoApiResponse.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            YesNoApiResponse yesNoApiResponse = response.getBody();
            if (yesNoApiResponse != null) {
                return yesNoApiResponse.getImage();
            }
        } else {
            log.error("Error fetching response from API: {}", response.getStatusCode());
        }
        return null;
    }

    @Override
    public StatsDTO getStats() {
        log.debug("api service: get stats");
        String apiUrl = BotConfig.API_BASE_URL + "/stats";
        try {
            ResponseEntity<StatsDTO> response = restTemplate.getForEntity(apiUrl, StatsDTO.class);
            log.debug("getting response in api service getStats");
            return response.getBody();
        } catch (Exception e) {
            log.error("Error while fetching statistics from API: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public QuoteDTO getRandomPublishedQuote() {
        log.debug("api service: getRandomPublishedQuote");
        String apiUrl = BotConfig.API_BASE_URL + "/quotes/random/published";
        ResponseEntity<QuoteDTO> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                null,
                QuoteDTO.class
        );
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            // Handle error or return null if no published quotes were found
            log.warn("No published quotes found.");
            return null;
        }
    }

    @Override
    public List<QuoteDTO> getWeekPublishedQuotes() {
        log.debug("api service: getWeekPublishedQuotes");
        String apiUrl = BotConfig.API_BASE_URL + "/quotes/week";
        ResponseEntity<QuoteDTO[]> responseEntity = restTemplate.getForEntity(apiUrl, QuoteDTO[].class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            log.debug("getWeekPublishedQuotes HttpStatus.OK");
            QuoteDTO[] quotesArray = responseEntity.getBody();
            if (quotesArray != null) {
                log.debug("return quotesArray");
                return Arrays.asList(quotesArray);
            }
        } else {
            log.warn("getWeekPublishedQuotes responseEntity.getStatusCode() is not OK");
        }
        return Collections.emptyList();
    }

    private String formatQuoteText(QuoteDTO quoteDTO) {
        return quoteDTO.getContent() + "\n\n"
                + quoteDTO.getBookAuthor() + "\n"
                + quoteDTO.getBookTitle();
    }

}
