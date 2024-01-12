package com.andmark.quotebot.service.impl;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.domain.RequestConfiguration;
import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.dto.*;
import com.andmark.quotebot.exception.QuoteException;
import com.andmark.quotebot.service.ApiService;
import com.andmark.quotebot.service.Bot;
import com.andmark.quotebot.util.BotAttributes;
import com.andmark.quotebot.service.keyboard.QuoteKeyboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
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
        log.debug("response.getStatusCode() = " + response.getStatusCode());

        QuoteDTO quoteDTO = response.getBody();

        if (quoteDTO.getContent() != null && quoteDTO.getContent().equals("No books available. Please scan the catalogue first.")) {
            log.debug("No books available. Please scan the catalogue first.");
            String errorMessage = quoteDTO.getContent();
            telegramBot.sendMessage(adminChatId, null, errorMessage);
            return null;
        } else if (response.getStatusCode() == HttpStatus.OK) {
            log.debug("getNextQuote HttpStatus.OK");
            log.info("get quote with id: {}", quoteDTO.getId());
            telegramBot.sendMessage(adminChatId, quoteKeyboardService.getEditKeyboardMarkup(quoteDTO.getId()), formatQuoteText(quoteDTO));
        } else {
            // Handle other status codes
            log.error("Unexpected response status code: {}", response.getStatusCode());
        }
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
    public UserDTO findUserByUsertgId(Long usertgId) {
        log.debug("getting user role for usertgId = {}", usertgId);
        ResponseEntity<UserDTO> response = restTemplate.getForEntity(BotConfig.API_BASE_URL + "/users/finduser-usertgid/" + usertgId, UserDTO.class);
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
        ResponseEntity<YesNoApiResponseDTO> response = restTemplate.exchange(
                apiUrl, HttpMethod.GET, null, YesNoApiResponseDTO.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            YesNoApiResponseDTO yesNoApiResponseDTO = response.getBody();
            if (yesNoApiResponseDTO != null) {
                return yesNoApiResponseDTO.getImage();
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
        log.debug("apiUrl = {}", apiUrl);
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

    @Override
    public String getRandomGreeting() {
        log.debug("api service: getRandomGreeting");
        String randomGreetingUrl = BotConfig.API_BASE_URL + "/greetings/random";
        ResponseEntity<String> response = restTemplate.getForEntity(randomGreetingUrl, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            log.debug("return greeting from response");
            return response.getBody();
        }
        return "";
    }

    @Override
    public String scanBooks(String directoryPath) {
        String message;
        ResponseEntity<List<BookDTO>> response = restTemplate.exchange(
                BotConfig.API_BASE_URL + "/scan-books?directoryPath=" + directoryPath,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BookDTO>>() {
                }
        );
        if (response.getStatusCode() == HttpStatus.OK) {
            List<BookDTO> scannedBooks = response.getBody();
            message = "Scanned books size:\n" + scannedBooks.size();
        } else {
            message = "Failed to scan books. Please check the directory path and try again.";
        }
        return message;
    }

    @Override
    public ScheduledActionStatusDTO getScheduledActionStatus() {
        log.debug("api service: getScheduledActionStatus");
        String scheduledActionStatusUrl = BotConfig.API_BASE_URL + "/scheduled/random";
        ResponseEntity<ScheduledActionStatusDTO> response = restTemplate.getForEntity(scheduledActionStatusUrl, ScheduledActionStatusDTO.class);
        log.debug("getting response");
        if (response.getStatusCode() == HttpStatus.OK) {
            log.debug("return schedulled action status from response");
            return response.getBody();
        } else {
            log.warn("response getStatusCode is not OK, return null");
            return null;
        }
    }

    @Override
    public void updateScheduledActionStatus(ScheduledActionStatusDTO scheduledActionStatusDTO) {
        log.debug("api service: updateScheduledActionStatus time: {}", scheduledActionStatusDTO.getLastExecuted());
        String scheduledActionStatusUrl = BotConfig.API_BASE_URL + "/scheduled/update";
        RequestConfiguration requestConfig = new RequestConfiguration.Builder()
                .url(scheduledActionStatusUrl)
                .httpMethod(HttpMethod.POST)
                .requestBody(scheduledActionStatusDTO)
                .chatId(adminChatId)
                .successMessage("Ежедневная цитата доставлена")
                .keyboard(null)
                .build();
        sendRequestAndHandleResponse(requestConfig);
    }

    @Override
    public ExtractedLinesDTO processPageAndLineNumber(Long userId, int pageNumber, int lineNumber) {
        log.debug("api service: processPageAndLineNumber userId = {}, pageNumber = {}, lineNumber = {}", userId, pageNumber, lineNumber);
        // Prepare the request body with page and line numbers
        PageLineRequestDTO requestDTO = new PageLineRequestDTO(pageNumber, lineNumber);
        String apiUrl = BotConfig.API_BASE_URL + "/books/process-page-and-line";
        log.debug("apiUrl = {}, requestDTO = {}",apiUrl, requestDTO);
        ResponseEntity<ExtractedLinesDTO> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                new HttpEntity<>(requestDTO),
                ExtractedLinesDTO.class
        );
        log.debug("response.getStatusCode() = {}", response.getStatusCode());
        if (response.getStatusCode() == HttpStatus.OK) {
            log.debug("api service: processPageAndLineNumber response status code is OK");
            return response.getBody();
        } else {
            log.warn("response getStatusCode is not OK, return null");
            return null;
        }
    }

    @Override
    public byte[] downloadImage(String imageUrl) {
        log.debug("downloadImage from imageUrl = {}", imageUrl);
        ResponseEntity<byte[]> response = restTemplate.exchange(
                imageUrl,
                HttpMethod.GET,
                null,
                byte[].class);
        return response.getBody();
    }

    @Override
    public AvailableDaysResponseDTO findAvailableDays() {
        log.debug("api service: send request findAvailableDays");
        String apiUrl = BotConfig.API_BASE_URL + "/quotes/get-available-days";

        ResponseEntity<AvailableDaysResponseDTO> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                null,
                AvailableDaysResponseDTO.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            log.debug("Response received with available days");
            AvailableDaysResponseDTO responseDTO = response.getBody();
            if (responseDTO != null) {
                log.debug("return response with available days list");
                return responseDTO;
            }
        } else {
            log.error("Error fetching available days. Status code: {}", response.getStatusCode());
        }
        return new AvailableDaysResponseDTO();
    }

    @Override
    public void addSuggestedQuote(QuoteDTO quoteDTO) {
        log.debug("api service: send request addSuggestedQuote");
        String apiUrl = BotConfig.API_BASE_URL + "/quotes/add-suggested";

        ResponseEntity<Void> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                new HttpEntity<>(quoteDTO),
                Void.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            log.debug("added suggested quote");
        } else {
            log.error("Error add suggested quote. Status code: {}", response.getStatusCode());
            telegramBot.sendMessage(quoteDTO.getUserId(), null, "Ошибка при добавлении цитаты. Попробуйте позже.");
            throw new QuoteException("Suggested quote do not added. Error.");
        }
    }

    private String formatQuoteText(QuoteDTO quoteDTO) {
        StringBuilder formattingText = new StringBuilder();
        formattingText.append(quoteDTO.getContent())
                .append("\n\n")
                .append(quoteDTO.getBookAuthor())
                .append("\n")
                .append(quoteDTO.getBookTitle());
        String resultText = formattingText.toString();
        int instagramLimitChar = 4096;
        int textLength = resultText.length();
        if (textLength > instagramLimitChar) {
            log.debug("text length: {}", textLength);
            resultText = resultText.substring(textLength - instagramLimitChar);
        }
        return resultText;
    }

}
