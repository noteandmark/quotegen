package com.andmark.quotebot.service.command;

import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.service.ApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DivinationCommandTest {
    @Mock
    private ApiService mockApiService;
    @Mock
    private AbsSender mockAbsSender;
    @Mock
    private Chat mockChat;

    private DivinationCommand divinationCommand;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        divinationCommand = new DivinationCommand(mockApiService);
        // Clear the user usage map before each test
        DivinationCommand.usersLastUsage.clear();
    }

    @Test
    public void testExecuteWithUserRole() throws TelegramApiException {
        User mockUser = new User();
        mockUser.setId(123L); // Set user ID
        when(mockApiService.getUserRole(anyLong())).thenReturn(UserRole.ROLE_USER);

        divinationCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        assertTrue(sentMessage.getText().contains("Загадали вопрос?"));
    }

    @Test
    public void testExecuteUserNotUsedCommandToday() throws TelegramApiException {
        User mockUser = new User();
        mockUser.setId(123L); // Set user ID
        when(mockApiService.getUserRole(anyLong())).thenReturn(UserRole.ROLE_ADMIN);

        // Ensure user hasn't used the command today
        DivinationCommand.usersLastUsage.clear();

        divinationCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        assertTrue(sentMessage.getText().contains("Напишите номер страницы:"));

        // Verify that user's usage has been marked
        assertTrue(DivinationCommand.usersLastUsage.containsKey(mockUser.getId()));
    }

    @Test
    public void testExecuteUserUsedCommandToday() throws TelegramApiException {
        User mockUser = new User();
        mockUser.setId(123L); // Set user ID
        when(mockApiService.getUserRole(anyLong())).thenReturn(UserRole.ROLE_ADMIN);

        // Set user's usage for today
        DivinationCommand.usersLastUsage.put(mockUser.getId(), LocalDate.now());

        divinationCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        assertTrue(sentMessage.getText().contains("Вы уже использовали команду \"Гадание по книге\" сегодня"));

        // Verify that user's usage remains unchanged
        assertTrue(DivinationCommand.usersLastUsage.containsKey(mockUser.getId()));
    }
}