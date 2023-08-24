package com.andmark.quotebot.service.command;

import com.andmark.quotebot.service.enums.BotState;
import com.andmark.quotebot.util.BotAttributes;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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

class ResetCommandTest {
    @Mock
    private BotAttributes mockBotAttributes;
    @Mock
    private AbsSender mockAbsSender;
    @Mock
    private Chat mockChat;

    @InjectMocks
    private ResetCommand resetCommand;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        resetCommand = new ResetCommand(mockBotAttributes);
    }

    @Test
    public void testExecute() throws TelegramApiException {
        User mockUser = new User();
        mockUser.setId(123L); // Set user ID

        resetCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        // Verify that the appropriate message is sent
        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        assertEquals(mockChat.getId(), Long.parseLong(sentMessage.getChatId()));
        assertTrue(sentMessage.getText().contains("Настройки сброшены до 'по умолчанию'"));
    }
}