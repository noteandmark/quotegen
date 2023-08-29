package com.andmark.quotebot.service;

import com.andmark.quotebot.dto.ScheduledActionStatusDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static com.andmark.quotebot.config.BotConfig.hoursScheduleExecution;

@Service
@Slf4j
public class ScheduledQuoteSenderService {
    private final ApiService apiService;

    public ScheduledQuoteSenderService(ApiService apiService) {
        this.apiService = apiService;
    }

// @Scheduled(fixedRate = 4 * 60 * 60 * 1000) // this line will be if we need scheduled to run every 4 hours

// Scheduled to run at 9 am, 12 pm, 3 pm, 6 pm, and 9 pm
    @Scheduled(cron = "0 0 9,12,15,18,21 * * *")
    public void sendQuoteToAdmin() {
        log.debug("scheduled run sendQuoteToAdmin check in {}", LocalDateTime.now());
        // Check if the action was already performed today
        ScheduledActionStatusDTO status = new ScheduledActionStatusDTO();
        status = apiService.getScheduledActionStatus();

        System.out.println("status = " + status);

        LocalDateTime lastExecuted = status.getLastExecuted();
        log.debug("get ScheduledActionStatusDTO status lastExecuted = {}", lastExecuted);

        if (lastExecuted == null || shouldExecute(lastExecuted)) {
            // Execute the action
            log.debug("lastExecuted check, call the method getNextQuote");
            apiService.getNextQuote();
            // Update the status
            LocalDateTime now = LocalDateTime.now();
            status.setLastExecuted(now);
            log.debug("update scheduled action status with time: {}", now);
            apiService.updateScheduledActionStatus(status);
        }
    }

    private boolean shouldExecute(LocalDateTime lastExecuted) {
        // Calculate the time difference in hours
        long hoursSinceLastExecution = lastExecuted.until(LocalDateTime.now(), ChronoUnit.HOURS);
        log.debug("hoursSinceLastExecution = {}", hoursSinceLastExecution);
        return hoursSinceLastExecution >= hoursScheduleExecution;
    }

}
