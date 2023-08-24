package com.andmark.quotebot.service.command;

import com.andmark.quotebot.service.enums.BotState;
import com.andmark.quotebot.util.BotAttributes;
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

class ReportCommandTest {
    @Mock
    private AbsSender mockAbsSender;

    @Mock
    private Chat mockChat;

    private ReportCommand reportCommand;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        reportCommand = new ReportCommand();
    }

    @Test
    public void testExecute() throws TelegramApiException {
        User mockUser = new User();
        mockUser.setId(123L); // Set user ID

        reportCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        assertEquals(mockChat.getId(), Long.parseLong(sentMessage.getChatId()));
        assertTrue(sentMessage.getText().contains("Отправьте мне сообщение, я его переправлю админу."));

        // Verify that the user's bot state has been set
        assertEquals(BotState.AWAITING_REPORT, BotAttributes.getUserCurrentBotState(mockUser.getId()));
    }
}