package com.andmark.quotegen.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile({"dev", "prod"}) // Both profiles will use this component
public class AppConfig {
    public static Integer quotesInDay;
    public static Integer daysPublicationAhead;
    public static Integer maxPeriodDaysAhead;

    @Value("${telegram.bot.quotes-in-day}")
    public void setQuotesInDay(Integer quotesInDay) {
        AppConfig.quotesInDay = quotesInDay;
    }

    @Value("${telegram.bot.days-publication-ahead}")
    public void setDaysPublicationAhead(Integer daysPublicationAhead) {
        AppConfig.daysPublicationAhead = daysPublicationAhead;
    }

    @Value("${telegram.bot.max-period-days-ahead}")
    public void setMaxPeriodDaysAhead(Integer maxPeriodDaysAhead) {
        AppConfig.maxPeriodDaysAhead = maxPeriodDaysAhead;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
