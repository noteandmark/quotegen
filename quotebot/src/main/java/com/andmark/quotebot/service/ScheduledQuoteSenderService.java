package com.andmark.quotebot.service;

import com.andmark.quotebot.dto.ScheduledActionStatusDTO;
import com.andmark.quotebot.exception.QuoteException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.Thread.sleep;

@Service
@Slf4j
public class ScheduledQuoteSenderService {
    @Autowired
    private ApiService apiService;

    @Scheduled(fixedRate = 8 * 60 * 60 * 1000) // Scheduled to run every 24 hours
    public void sendQuoteToAdmin() {
        try {
            log.debug("scheduled run send quote to admin");
            sleep(3000);
        } catch (InterruptedException e) {
            log.error("InterruptedException in scheduled send quote: " + e.getMessage());
            throw new QuoteException("InterruptedException in scheduled send quote");
        }
        // Check if the action was already performed today
        ScheduledActionStatusDTO status = apiService.getScheduledActionStatus();
        log.debug("get ScheduledActionStatusDTO status = {}", status.getLastExecuted());
        LocalDateTime lastExecuted = status.getLastExecuted();
        LocalDate today = LocalDate.now();
        if (lastExecuted == null || lastExecuted.toLocalDate().isBefore(today)) {
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
}
