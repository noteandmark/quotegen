package com.andmark.quotebot.service.command;

import com.andmark.quotebot.service.UserService;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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

class SignOutCommandTest {
    @Mock
    private UserService mockUserService;
    @Mock
    private AbsSender mockAbsSender;
    @Mock
    private Chat mockChat;

    @InjectMocks
    private SignOutCommand signOutCommand;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        signOutCommand = new SignOutCommand(mockUserService);
    }

    @Test
    public void testExecuteWithRegisteredUser() throws TelegramApiException {
        User mockUser = new User();
        mockUser.setId(123L); // Set user ID
        when(mockUserService.isRegistered(anyLong())).thenReturn(true);

        signOutCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        // Verify that the appropriate methods are called
        verify(mockUserService, times(1)).deleteUser(anyLong(), anyLong());
        verify(mockUserService, times(1)).initiateRegistration(anyLong(), anyLong());

        // Verify that the sendMessage method is called
        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        assertEquals(mockChat.getId(), Long.parseLong(sentMessage.getChatId()));
    }

    @Test
    public void testExecuteWithUnregisteredUser() throws TelegramApiException {
        User mockUser = new User();
        mockUser.setId(123L); // Set user ID
        when(mockUserService.isRegistered(anyLong())).thenReturn(false);

        signOutCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        // Verify that the appropriate method is not called
        verify(mockUserService, never()).deleteUser(anyLong(), anyLong());
        verify(mockUserService, never()).initiateRegistration(anyLong(), anyLong());

        // Verify that the sendMessage method is called
        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        assertEquals(mockChat.getId(), Long.parseLong(sentMessage.getChatId()));
        assertTrue(sentMessage.getText().contains("Вы еще не зарегистрированы."));
    }
}