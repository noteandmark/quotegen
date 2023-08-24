package com.andmark.quotebot.service.command;

import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.service.ApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartCommandTest {
    @Mock
    private ApiService mockApiService;
    @Mock
    private AbsSender mockAbsSender;
    @Mock
    private User mockUser;
    @Mock
    private Chat mockChat;

    private StartCommand startCommand;

    @BeforeEach
    public void setUp() {
        startCommand = new StartCommand(mockApiService);
    }

    @Test
    public void testExecuteWithUserRole() throws TelegramApiException {
        when(mockUser.getFirstName()).thenReturn("John");
        when(mockApiService.getUserRole(anyLong())).thenReturn(UserRole.USER);

        startCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        String expectedText = "Добро пожаловать! Меня зовут бот Книголюб. Я в сети.\n" +
                "Используй /help для просмотра меню.\n" + "Приветствую, " + mockUser.getFirstName() + "!";
        assertEquals(expectedText, sentMessage.getText());
    }

    @Test
    public void testExecuteWithoutUserRole() throws TelegramApiException {
        when(mockUser.getFirstName()).thenReturn("Alice");
        when(mockApiService.getUserRole(anyLong())).thenReturn(null);

        startCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        String expectedText = "Добро пожаловать! Меня зовут бот Книголюб. Я в сети.\n" +
                "Используй /help для просмотра меню.\n" + "Зарегистрируйся (/signup), чтобы получить доступ ко всем возможностям :)";
        assertEquals(expectedText, sentMessage.getText());
    }


}