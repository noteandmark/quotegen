package com.andmark.quotebot.service;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.dto.QuoteDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class QuoteApiService {

    private final RestTemplate restTemplate;

    public QuoteApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<QuoteDTO> getPendingQuotes() {
        ResponseEntity<QuoteDTO[]> response = restTemplate.getForEntity(BotConfig.API_BASE_URL + "/quotes/pending", QuoteDTO[].class);
        log.debug("get response from restTemplate.getForEntity");
        return Arrays.asList(response.getBody());
    }

    public boolean publishQuoteToGroup(String content, byte[] imageBytes) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        if (imageBytes != null && imageBytes.length > 0) {
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chatId);
            sendPhoto.setPhoto(new InputFile(new ByteArrayInputStream(imageBytes), "image.jpg"));
            sendPhoto.setCaption(content);

            try {
                SendPhotoResponse response = execute(sendPhoto);
                return response != null && response.isOk();
            } catch (TelegramApiException e) {
                log.error("Error publishing photo to group: {}", e.getMessage());
                return false;
            }
        } else {
            sendMessage.setText(content);

            try {
                SendMessageResponse response = execute(sendMessage);
                return response != null && response.isOk();
            } catch (TelegramApiException e) {
                log.error("Error publishing message to group: {}", e.getMessage());
                return false;
            }
        }
    }
}
