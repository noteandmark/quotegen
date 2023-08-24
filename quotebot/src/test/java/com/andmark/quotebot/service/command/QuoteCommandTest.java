package com.andmark.quotebot.service.command;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class QuoteCommandTest {
    @Mock
    private AbsSender mockAbsSender;

    @Mock
    private Chat mockChat;

    private TestQuoteCommand testQuoteCommand;

    private static class TestQuoteCommand extends QuoteCommand {
        public TestQuoteCommand(String commandIdentifier, String description) {
            super(commandIdentifier, description);
        }

        @Override
        public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
            // Do nothing, this is just for testing
        }
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testQuoteCommand = new TestQuoteCommand("test", "Test command");
    }

    @Test
    public void testSendMessage() throws TelegramApiException {
        String messageText = "This is a test message";

        testQuoteCommand.sendMessage(mockAbsSender, mockChat, messageText);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        assertEquals(mockChat.getId(), Long.parseLong(sentMessage.getChatId()));
        assertEquals(messageText, sentMessage.getText());
    }

}