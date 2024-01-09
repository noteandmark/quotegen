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
    public static Integer QUOTES_IN_DAY;
    public static Integer daysPublicationAhead;
    public static Integer maxPeriodDaysAhead;
    public static long MIN_TIME_THRESHOLD;
    public static String ngrokAuthToken;


    @Value("${telegram.bot.quotes-in-day}")
    public void setQuotesInDay(Integer quotesInDay) {
        AppConfig.QUOTES_IN_DAY = quotesInDay;
    }

    @Value("${telegram.bot.days-publication-ahead}")
    public void setDaysPublicationAhead(Integer daysPublicationAhead) {
        AppConfig.daysPublicationAhead = daysPublicationAhead;
    }

    @Value("${telegram.bot.max-period-days-ahead}")
    public void setMaxPeriodDaysAhead(Integer maxPeriodDaysAhead) {
        AppConfig.maxPeriodDaysAhead = maxPeriodDaysAhead;
    }

    @Value("${app.minTimeThreshold}")
    public void setMinTimeThreshold(long minTimeThreshold) {
        AppConfig.MIN_TIME_THRESHOLD = minTimeThreshold;
    }

    @Value("${NGROK_AUTHTOKEN}")
    public void setNgrokAuthToken(String ngrokAuthToken) {AppConfig.ngrokAuthToken = ngrokAuthToken;}

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
