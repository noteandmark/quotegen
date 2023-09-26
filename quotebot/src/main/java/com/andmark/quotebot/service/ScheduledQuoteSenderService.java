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

    public void sendQuoteToAdmin() {
        log.debug("scheduled run sendQuoteToAdmin check in {}", LocalDateTime.now());
        // Check if the action was already performed today
        ScheduledActionStatusDTO status = new ScheduledActionStatusDTO();
        status = apiService.getScheduledActionStatus();

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
