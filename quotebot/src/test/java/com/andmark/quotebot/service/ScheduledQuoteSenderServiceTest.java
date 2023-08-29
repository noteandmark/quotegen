package com.andmark.quotebot.service;

import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.dto.ScheduledActionStatusDTO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
        "hoursScheduleExecution=8"
})
public class ScheduledQuoteSenderServiceTest {
    @Mock
    private ApiService apiService;

    @InjectMocks
    private ScheduledQuoteSenderService scheduledQuoteSenderService;

    @Value("${telegram.bot.hours-schedule-execution}")
    public Integer hoursScheduleExecution;

    @Test
    public void testSendQuoteToAdmin_ShouldExecute() {
        // Given
        ScheduledActionStatusDTO status = new ScheduledActionStatusDTO();
        status.setLastExecuted(LocalDateTime.now().minusHours(10)); // last executed 10 hours ago

        when(apiService.getScheduledActionStatus()).thenReturn(status);
        when(apiService.getNextQuote()).thenReturn(new QuoteDTO());

        // When
        scheduledQuoteSenderService.sendQuoteToAdmin();

        // Then
        verify(apiService, times(1)).getNextQuote();
        verify(apiService, times(1)).updateScheduledActionStatus(any(ScheduledActionStatusDTO.class));
    }

    @Test
    public void testSendQuoteToAdmin_ShouldNotExecute() {
        // Given
        ScheduledActionStatusDTO mockStatus = new ScheduledActionStatusDTO();
        mockStatus.setLastExecuted(LocalDateTime.now().minusHours(1)); // last executed 1 hour ago

        System.out.println("status in test = " + mockStatus);
        // When
        when(apiService.getScheduledActionStatus()).thenReturn(mockStatus);

        scheduledQuoteSenderService.sendQuoteToAdmin();

        // Then
        verify(apiService, never()).getNextQuote();
        verify(apiService, never()).updateScheduledActionStatus(any(ScheduledActionStatusDTO.class));
    }
}