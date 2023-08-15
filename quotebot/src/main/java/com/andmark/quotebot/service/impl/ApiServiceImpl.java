package com.andmark.quotebot.service.impl;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.dto.UserDTO;
import com.andmark.quotebot.service.ApiService;
import com.andmark.quotebot.service.Bot;
import com.andmark.quotebot.service.BotAttributes;
import com.andmark.quotebot.service.keyboard.QuoteKeyboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    public void registerUser(UserDTO userDTO) {
        log.debug("api service request: registerUser with username = {}", userDTO.getUsername());
        // Hash the password before sending it to the API
        String hashedPassword = passwordEncoder.encode(userDTO.getPassword());
        userDTO.setPassword(hashedPassword);

        String registerUserUrl = BotConfig.API_BASE_URL + "/users/register";
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    registerUserUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(userDTO),
                    Void.class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("User registration successful");
            } else {
                log.error("Failed to register user. Status code: {}", response.getStatusCode());
            }
        } catch (HttpClientErrorException ex) {
            log.warn("HttpClientErrorException while sending user registration request: {}", ex.getMessage());
        }
    }

    @Override
    public void sendRequestAndHandleResponse(String url, HttpMethod httpMethod, Object requestBody, String successMessage, InlineKeyboardMarkup keyboard) {
        log.debug("api service sendRequestAndHandleResponse");
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
