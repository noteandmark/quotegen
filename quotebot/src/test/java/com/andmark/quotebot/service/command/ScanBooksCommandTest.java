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

class ScanBooksCommandTest {
    @Mock
    private ApiService mockApiService;
    @Mock
    private AbsSender mockAbsSender;
    @Mock
    private Chat mockChat;

    private ScanBooksCommand scanBooksCommand;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        scanBooksCommand = new ScanBooksCommand(mockApiService);
    }

    @Test
    public void testExecuteWithAdminRole() throws TelegramApiException {
        User mockUser = new User();
        mockUser.setId(123L); // Set user ID
        when(mockApiService.getUserRole(anyLong())).thenReturn(UserRole.ADMIN);

        String[] arguments = {"path/to/books"};
        // Mock the apiService.scanBooks method to return a non-null value
        when(mockApiService.scanBooks(anyString())).thenReturn("Books scanning completed.");

        scanBooksCommand.execute(mockAbsSender, mockUser, mockChat, arguments);

        // Verify that the appropriate message is sent
        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        assertEquals(mockChat.getId(), Long.parseLong(sentMessage.getChatId()));

        // Verify that scanBooks method is called
        verify(mockApiService, times(1)).scanBooks("path/to/books");
    }

    @Test
    public void testExecuteWithoutAdminRole() throws TelegramApiException {
        User mockUser = new User();
        mockUser.setId(123L); // Set user ID
        when(mockApiService.getUserRole(anyLong())).thenReturn(UserRole.USER);

        String[] arguments = {"path/to/books"};

        scanBooksCommand.execute(mockAbsSender, mockUser, mockChat, arguments);

        // Verify that the appropriate message is sent
        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        assertEquals(mockChat.getId(), Long.parseLong(sentMessage.getChatId()));
        assertTrue(sentMessage.getText().contains("Эта возможность только для админов"));

        // Verify that scanBooks method is not called
        verify(mockApiService, never()).scanBooks(any());
    }
}