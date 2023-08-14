package com.andmark.quotebot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BotConfig {
    public static String API_BASE_URL;
    public static String botUsername;
    public static String botToken;
    public static String groupChatId;

    @Value("${api.base.url}")
    public void setApiBaseUrl(String API_BASE_URL) {
        BotConfig.API_BASE_URL = API_BASE_URL;
    }

    @Value("${telegram.bot.username}")
    public void setBotUsername(String botUsername) {
        BotConfig.botUsername = botUsername;
    }

    @Value("${telegram.bot.token}")
    public void setBotToken(String botToken) {
        BotConfig.botToken = botToken;
    }

    @Value("${telegram.bot.groupid}")
    public void setGroupChatId(String groupChatId) {
        BotConfig.groupChatId = groupChatId;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
