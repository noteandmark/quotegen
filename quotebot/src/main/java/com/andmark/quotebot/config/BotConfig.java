package com.andmark.quotebot.config;

import com.andmark.quotebot.domain.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Profile({"dev", "prod"}) // Both profiles will use this component
public class BotConfig {
    public static String API_BASE_URL;
    public static String botUsername;
    public static String botToken;
    public static Long groupChatId;
    public static Long adminChatId;
    public static String readmeFile;
    public static String changelogFile;
    public static Integer hoursScheduleExecution;

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
    public void setGroupChatId(Long groupChatId) {
        BotConfig.groupChatId = groupChatId;
    }

    @Value("${telegram.bot.adminchatid}")
    public void setAdminChatId(Long adminChatId) {
        BotConfig.adminChatId = adminChatId;
    }

    @Value("${readme.file}")
    public void setReadmeFile(String readmeFile) {
        BotConfig.readmeFile = readmeFile;
    }

    @Value("${changelog.file}")
    public void setChangelogFile(String changelogFile) {
        BotConfig.changelogFile = changelogFile;
    }

    @Value("${telegram.bot.hours-schedule-execution}")
    public void setHoursScheduleExecution(Integer hoursScheduleExecution) {
        BotConfig.hoursScheduleExecution = hoursScheduleExecution;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public Map<Long, UserRole> userRoleCache() {
        return new ConcurrentHashMap<>();
    }

}
