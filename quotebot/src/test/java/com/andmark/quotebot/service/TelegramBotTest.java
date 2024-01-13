package com.andmark.quotebot.service;


import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.service.command.HelpCommand;
import com.andmark.quotebot.service.command.QuoteCommand;
import com.andmark.quotebot.service.command.StartCommand;
import com.andmark.quotebot.service.command.VersionCommand;
import com.andmark.quotebot.service.enums.BotState;
import com.andmark.quotebot.util.BotAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class TelegramBotTest {
    @Mock
    private BotAttributes botAttributes;
    @Mock
    private BotConfig botConfigMock;
    @Mock
    private ApiService apiService;
    @Mock
    private QuoteService quoteService;
    @Mock
    private UserService userService;

    @InjectMocks
    private TelegramBot telegramBot;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessNonCommandUpdate_UnregisteredUser_thenShouldDoNothing() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Mocking the static method getUserCurrentBotState
            mockedBotAttributes.when(() -> BotAttributes.getUserCurrentBotState(anyLong()))
                    .thenReturn(BotState.START);
            Update update = createUpdateWithMessageText("some request");

            when(userService.isRegistered(123L)).thenReturn(false);

            telegramBot.processNonCommandUpdate(update);

            verifyNoMoreInteractions(quoteService, apiService);
        }
    }

    @Test
    public void testProcessNonCommandUpdate_RegisteredUser() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Mocking the static method getUserCurrentBotState
            mockedBotAttributes.when(() -> BotAttributes.getUserCurrentBotState(123L))
                    .thenReturn(BotState.START);

            Update update = createUpdateWithMessageText("some request");

            when(userService.isRegistered(123L)).thenReturn(true);
            when(apiService.getUserRole(123L)).thenReturn(UserRole.ROLE_USER);
            doNothing().when(quoteService).handleIncomingMessage(update);

            telegramBot.processNonCommandUpdate(update);

            verify(quoteService).handleIncomingMessage(update);
            verify(apiService).getUserRole(123L);
            verifyNoMoreInteractions(quoteService);
        }
    }

    @Test
    public void testProcessNonCommandUpdate_whenAwaitingUserNameInput_thenShouldCallHandleUsernameInputResponse() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Mocking the static method getUserCurrentBotState
            mockedBotAttributes.when(() -> BotAttributes.getUserCurrentBotState(123L))
                    .thenReturn(BotState.AWAITING_USERNAME_INPUT);

            Update update = createUpdateWithMessageText("some request");

            when(userService.isRegistered(eq(123L))).thenReturn(false);
            when(apiService.getUserRole(123L)).thenReturn(UserRole.ROLE_USER);
            doNothing().when(quoteService).handleUsernameInputResponse(eq(update));
            doNothing().when(quoteService).handleIncomingMessage(eq(update));

            telegramBot.processNonCommandUpdate(update);

            verify(quoteService).handleUsernameInputResponse(update);
            verify(quoteService).handleIncomingMessage(update);
            verify(userService).isRegistered(123L);
            verifyNoMoreInteractions(quoteService, userService, apiService);
        }
    }

    @Test
    public void testProcessNonCommandUpdate_whenAwaitingPasswordInput_thenShouldCallHandlePasswordInputResponse() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Mocking the static method getUserCurrentBotState
            mockedBotAttributes.when(() -> BotAttributes.getUserCurrentBotState(123L))
                    .thenReturn(BotState.AWAITING_PASSWORD_INPUT);

            Update update = createUpdateWithMessageText("some request");

            when(userService.isRegistered(123L)).thenReturn(false);
            when(apiService.getUserRole(123L)).thenReturn(UserRole.ROLE_USER);
            doNothing().when(quoteService).handlePasswordInputResponse(update);
            doNothing().when(quoteService).handleIncomingMessage(update);

            telegramBot.processNonCommandUpdate(update);

            verify(quoteService).handlePasswordInputResponse(update);
            verify(quoteService).handleIncomingMessage(update);
            verify(userService).isRegistered(123L);
            verifyNoMoreInteractions(quoteService, userService, apiService);
        }
    }

    @Test
    public void testProcessNonCommandUpdate_whenAwaitingPasswordInput_thenShouldCallHandleReportInputResponse() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Mocking the static method getUserCurrentBotState
            mockedBotAttributes.when(() -> BotAttributes.getUserCurrentBotState(123L))
                    .thenReturn(BotState.AWAITING_REPORT);
            String reportMessage = "report message";
            Update update = createUpdateWithMessageText(reportMessage);

            when(userService.isRegistered(123L)).thenReturn(false);
            when(apiService.getUserRole(123L)).thenReturn(UserRole.ROLE_USER);
            doNothing().when(quoteService).handleReportInput(eq(update.getMessage().getFrom()),eq(reportMessage));
            doNothing().when(quoteService).handleIncomingMessage(update);

            telegramBot.processNonCommandUpdate(update);

            verify(quoteService).handleReportInput(update.getMessage().getFrom(),reportMessage);
            verify(quoteService).handleIncomingMessage(update);
            verify(userService).isRegistered(123L);
            verifyNoMoreInteractions(quoteService, userService, apiService);
        }
    }

    @Test
    public void testProcessNonCommandUpdate_CallbackQuery() {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        Message message = new Message();
        message.setText("some callback message");
        callbackQuery.setMessage(message);
        update.setCallbackQuery(callbackQuery);

        doNothing().when(quoteService).handleCallbackQuery(update);

        telegramBot.processNonCommandUpdate(update);

        verify(quoteService).handleCallbackQuery(update);
        verifyNoMoreInteractions(quoteService, userService, apiService);
    }

    @Test
    public void testCheckAndPublishPendingQuotes_NoPendingQuotes() {
        List<QuoteDTO> emptyList = Collections.emptyList();
        when(apiService.getPendingQuotes()).thenReturn(emptyList);

        telegramBot.checkAndPublishPendingQuotes();

        verify(apiService).getPendingQuotes();
        verifyNoMoreInteractions(apiService, quoteService);
    }

    @Test
    public void testCheckAndPublishPendingQuotes_PendingQuotesToPublish() {
        LocalDateTime currentDateTime = LocalDateTime.of(2023, 8, 26, 12, 0);

        List<QuoteDTO> pendingQuotes = new ArrayList<>();
        QuoteDTO quoteToPublish = new QuoteDTO();
        quoteToPublish.setId(1L);
        quoteToPublish.setPendingTime(currentDateTime.minusMinutes(30)); // Publish 30 minutes ago
        pendingQuotes.add(quoteToPublish);

        when(apiService.getPendingQuotes()).thenReturn(pendingQuotes);
        when(quoteService.publishQuoteToGroup(eq(quoteToPublish))).thenReturn(quoteToPublish);

        telegramBot.checkAndPublishPendingQuotes();

        verify(apiService).getPendingQuotes();
        verify(quoteService).publishQuoteToGroup(eq(quoteToPublish));
        verify(quoteService).sendQuoteSavedTODatabase(eq(quoteToPublish), anyString());
        verifyNoMoreInteractions(apiService, quoteService);
    }

    @Test
    public void testCheckAndPublishPendingQuotes_PendingQuotesNotReadyToPublish() {
        LocalDateTime currentDateTime = LocalDateTime.now();

        List<QuoteDTO> pendingQuotes = new ArrayList<>();
        QuoteDTO quoteNotReady = new QuoteDTO();
        quoteNotReady.setId(2L);
        quoteNotReady.setPendingTime(currentDateTime.plusHours(2)); // Not ready to publish yet
        pendingQuotes.add(quoteNotReady);

        when(apiService.getPendingQuotes()).thenReturn(pendingQuotes);

        telegramBot.checkAndPublishPendingQuotes();

        verify(apiService).getPendingQuotes();
        verifyNoMoreInteractions(apiService, quoteService);
    }

    private Update createUpdateWithMessageText(String text) {
        Chat chat = new Chat();
        chat.setId(789L);

        Message message = new Message();
        message.setChat(chat);
        message.setMessageId(456);
        message.setText(text);

        User user = new User();
        user.setId(123L);
        user.setUserName("Some user");
        message.setFrom(user);

        Update update = new Update();
        update.setMessage(message);

        return update;
    }

    private User createUserMock(long id, String userName) {
        User user = new User();
        user.setId(id);
        user.setUserName(userName);
        return user;
    }

}