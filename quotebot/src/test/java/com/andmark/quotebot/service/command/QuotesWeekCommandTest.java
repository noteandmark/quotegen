package com.andmark.quotebot.service.command;

import com.andmark.quotebot.domain.enums.QuoteStatus;
import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.service.ApiService;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class QuotesWeekCommandTest {
    @Mock
    private ApiService mockApiService;

    @Mock
    private AbsSender mockAbsSender;

    @Mock
    private Chat mockChat;

    private QuotesWeekCommand quotesWeekCommand;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        quotesWeekCommand = new QuotesWeekCommand(mockApiService);
    }

    @Test
    public void testExecuteWithPublishedQuotes() throws TelegramApiException {
        User mockUser = new User();
        mockUser.setId(123L); // Set user ID
        when(mockApiService.getUserRole(anyLong())).thenReturn(UserRole.ROLE_ADMIN);

        // Prepare a list of QuoteDTOs
        List<QuoteDTO> quoteDTOs = new ArrayList<>();

        quoteDTOs.add(new QuoteDTO(1L, "content 1", QuoteStatus.FREE, LocalDateTime.now(), null, LocalDateTime.now(), null, "author1", "title1", null));
        quoteDTOs.add(new QuoteDTO(2L, "content 2", QuoteStatus.FREE, LocalDateTime.now(), null, LocalDateTime.now(), null, "author2", "title2", null));
        when(mockApiService.getWeekPublishedQuotes()).thenReturn(quoteDTOs);

        quotesWeekCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(2)).execute(sendMessageCaptor.capture());

        List<SendMessage> sentMessages = sendMessageCaptor.getAllValues();
        for (int i = 0; i < quoteDTOs.size(); i++) {
            assertEquals(quoteDTOs.get(i).getContent(), sentMessages.get(i).getText());
        }
    }

    @Test
    public void testExecuteWithNoPublishedQuotes() throws TelegramApiException {
        User mockUser = new User();
        mockUser.setId(123L); // Set user ID
        when(mockApiService.getUserRole(anyLong())).thenReturn(UserRole.ROLE_ADMIN);

        when(mockApiService.getWeekPublishedQuotes()).thenReturn(null);

        quotesWeekCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        assertTrue(sentMessage.getText().contains("К сожалению, или нет опубликованных цитат за неделю, или какая-то ошибка на сервере."));
    }
}