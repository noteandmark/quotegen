package com.andmark.quotebot.service.command;

import com.andmark.quotebot.domain.enums.UserRole;
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

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class RequestQuoteCommandTest {
    @Mock
    private ApiService mockApiService;
    @Mock
    private AbsSender mockAbsSender;
    @Mock
    private Chat mockChat;

    private RequestQuoteCommand requestQuoteCommand;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        requestQuoteCommand = new RequestQuoteCommand(mockApiService);
    }

    @Test
    public void testExecuteWithAdminRole() throws TelegramApiException {
        User mockUser = new User();
        mockUser.setId(123L); // Set user ID
        when(mockApiService.getUserRole(anyLong())).thenReturn(UserRole.ADMIN);

        requestQuoteCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        // Verify that the API service's getNextQuote() method is called
        verify(mockApiService, times(1)).getNextQuote();

        // Make sure no sendMessage is called for admins
        verify(mockAbsSender, never()).execute(any(SendMessage.class));
    }

    @Test
    public void testExecuteWithoutAdminRole() throws TelegramApiException {
        User mockUser = new User();
        mockUser.setId(123L); // Set user ID
        when(mockApiService.getUserRole(anyLong())).thenReturn(UserRole.USER);

        requestQuoteCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        // Verify that the API service's getNextQuote() method is not called
        verify(mockApiService, never()).getNextQuote();

        // Verify that the appropriate message is sent to non-admin users
        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        assertTrue(sentMessage.getText().contains("Эта возможность только для админов"));
    }
}