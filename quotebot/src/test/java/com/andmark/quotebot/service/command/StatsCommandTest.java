package com.andmark.quotebot.service.command;

import com.andmark.quotebot.dto.StatsDTO;
import com.andmark.quotebot.service.ApiService;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class StatsCommandTest {
    @Mock
    private ApiService mockApiService;
    @Mock
    private AbsSender mockAbsSender;
    @Mock
    private Chat mockChat;

    private StatsCommand statsCommand;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        statsCommand = new StatsCommand(mockApiService);
    }

    @Test
    public void testExecuteSuccess() throws TelegramApiException {
        User mockUser = new User();
        mockUser.setId(123L); // Set user ID

        StatsDTO mockStats = new StatsDTO();
        mockStats.setBookCount(10L);
        mockStats.setPublishedQuotesThisYear(100L);
        mockStats.setPendingQuotesCount(5L);

        when(mockApiService.getStats()).thenReturn(mockStats);

        statsCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        assertEquals(mockChat.getId(), Long.parseLong(sentMessage.getChatId()));
        assertTrue(sentMessage.getText().contains("Статистика"));
        assertTrue(sentMessage.getText().contains("Количество книг в каталоге: 10"));
        assertTrue(sentMessage.getText().contains("Количество опубликованных Книголюбом цитат за год: 100"));
        assertTrue(sentMessage.getText().contains("Количество ожидаемых к публикации цитат: 5"));
    }

    @Test
    public void testExecuteFailure() throws TelegramApiException {
        User mockUser = new User();
        mockUser.setId(123L); // Set user ID

        when(mockApiService.getStats()).thenThrow(new RuntimeException("API Error"));

        statsCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        assertEquals(mockChat.getId(), Long.parseLong(sentMessage.getChatId()));
        assertTrue(sentMessage.getText().contains("An error occurred while retrieving statistics."));
    }
}