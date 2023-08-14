package com.andmark.quotebot.service;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.dto.QuoteDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ApiService {

    private final BotAttributes botAttributes;
    private final RestTemplate restTemplate;

    public ApiService(BotAttributes botAttributes, RestTemplate restTemplate) {
        this.botAttributes = botAttributes;
        this.restTemplate = restTemplate;
    }

    public List<QuoteDTO> getPendingQuotes() {
        ResponseEntity<QuoteDTO[]> response = restTemplate.getForEntity(BotConfig.API_BASE_URL + "/quotes/get-pending", QuoteDTO[].class);
        log.debug("get response from restTemplate.getForEntity");
        return Arrays.asList(response.getBody());
    }

    public boolean publishQuoteToGroup(String content, byte[] imageBytes) {
//        Long chatId = botAttributes.getChatId();
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setChatId(chatId);

//        if (imageBytes != null && imageBytes.length > 0) {
//            SendPhoto sendPhoto = new SendPhoto();
//            sendPhoto.setChatId(chatId);
//            sendPhoto.setPhoto(new InputFile(new ByteArrayInputStream(imageBytes), "image.jpg"));
//            sendPhoto.setCaption(content);
//
//            try {
//                SendPhotoResponse response = execute(sendPhoto);
//                return response != null && response.isOk();
//            } catch (TelegramApiException e) {
//                log.error("Error publishing photo to group: {}", e.getMessage());
//                return false;
//            }
//        } else {
//            sendMessage.setText(content);
//
//            try {
//                SendMessageResponse response = execute(sendMessage);
//                return response != null && response.isOk();
//            } catch (TelegramApiException e) {
//                log.error("Error publishing message to group: {}", e.getMessage());
//                return false;
//            }
//        }
        return false;
    }

}
