package com.andmark.quotebot.service.command;

import com.andmark.quotebot.service.ApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class YesNoMagicCommandTest {
    @Mock
    private ApiService mockApiService;
    @Mock
    private AbsSender mockAbsSender;
    @Mock
    private User mockUser;
    @Mock
    private Chat mockChat;

    private YesNoMagicCommand yesNoMagicCommand;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        yesNoMagicCommand = new YesNoMagicCommand(mockApiService);
    }

    @Test
    public void testExecute() throws TelegramApiException {
        when(mockApiService.getResponseYesOrNo(anyString())).thenReturn("https://example.com/yes.gif");

        yesNoMagicCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        // Verify that absSender.execute has been called with the appropriate arguments
        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        ArgumentCaptor<SendAnimation> sendAnimationCaptor = ArgumentCaptor.forClass(SendAnimation.class);
        verify(mockAbsSender, times(1)).execute(sendAnimationCaptor.capture());

    }
}