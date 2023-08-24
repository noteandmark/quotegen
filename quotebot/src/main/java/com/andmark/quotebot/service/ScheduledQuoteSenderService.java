package com.andmark.quotebot.service;

import com.andmark.quotebot.dto.ScheduledActionStatusDTO;
import com.andmark.quotebot.exception.QuoteException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static java.lang.Thread.sleep;

@Service
@Slf4j
public class ScheduledQuoteSenderService {
    @Autowired
    private ApiService apiService;

    //    @Scheduled(fixedRate = 4 * 60 * 60 * 1000) // Scheduled to run every 4 hours
// Scheduled to run at 9 am, 12 pm, 3 pm, 6 pm, and 9 pm
    @Scheduled(cron = "0 0 9,12,15,18,21 * * *")
    public void sendQuoteToAdmin() {
        log.debug("scheduled run sendQuoteToAdmin check in {}", LocalDateTime.now());
        // Check if the action was already performed today
        ScheduledActionStatusDTO status = apiService.getScheduledActionStatus();
        log.debug("get ScheduledActionStatusDTO status = {}", status.getLastExecuted());

        LocalDateTime lastExecuted = status.getLastExecuted();

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
        // TODO: вынести в настройки приложения !!!!!!!!!!
        return hoursSinceLastExecution >= 8;
    }

}
