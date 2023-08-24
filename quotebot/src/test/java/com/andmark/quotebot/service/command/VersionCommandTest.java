package com.andmark.quotebot.service.command;

import com.andmark.quotebot.config.BotConfig;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "readmeFile=static/version_info/readme.txt",
        "changelogFile=static/version_info/CHANGELOG.md"
})
class VersionCommandTest {
    @Mock
    private AbsSender mockAbsSender;
    @Mock
    private Chat mockChat;

    private VersionCommand versionCommand;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        versionCommand = spy(new VersionCommand());
    }

    @Test
    public void testExecuteWithContent() throws TelegramApiException {
        User mockUser = new User();
        mockUser.setId(123L); // Set user ID

        // Mock the loadFileContent method
        doReturn("Mock Readme Content").when(versionCommand).loadFileContent(anyString());

        versionCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        assertEquals(mockChat.getId(), Long.parseLong(sentMessage.getChatId()));
    }

    @Test
    public void testExecuteWithEmptyContent() throws TelegramApiException {
        User mockUser = new User();
        mockUser.setId(123L); // Set user ID

        doReturn("").when(versionCommand).loadFileContent(anyString());

        versionCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        assertEquals(mockChat.getId(), Long.parseLong(sentMessage.getChatId()));
        assertTrue(sentMessage.getText().contains("readme и changelog недоступны"));
    }

}