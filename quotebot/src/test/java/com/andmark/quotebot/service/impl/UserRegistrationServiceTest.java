package com.andmark.quotebot.service.impl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.dto.UserDTO;
import com.andmark.quotebot.service.Bot;
import com.andmark.quotebot.service.enums.BotState;
import com.andmark.quotebot.util.BotAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

public class UserRegistrationServiceTest {
    @Mock
    private Bot telegramBot;
    @Mock
    private BotAttributes botAttributes;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInitiateRegistration_NotInProgress() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Mocking the static method getUserCurrentBotState
            mockedBotAttributes.when(() -> BotAttributes.getUserCurrentBotState(anyLong()))
                    .thenReturn(null);
            // Given
            Long userId = 123L;
            Long chatId = 456L;

            // When
            userRegistrationService.initiateRegistration(userId, chatId);

            // Then
            verify(telegramBot, times(1)).sendMessage(eq(chatId), eq(null), eq("Введите имя (логин) пользователя"));
        }
    }

    @Test
    public void testInitiateRegistration_InProgress() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Mocking the static method getUserCurrentBotState
            mockedBotAttributes.when(() -> BotAttributes.getUserCurrentBotState(anyLong()))
                    .thenReturn(BotState.AWAITING_USERNAME_INPUT);
            // Given
            Long userId = 123L;
            Long chatId = 456L;
            // Mocking the usersInProgress set behavior
            UserRegistrationService spyUserService = spy(userRegistrationService);
            spyUserService.setUsersInProgress(userId);
            // When
            spyUserService.initiateRegistration(userId, chatId);

            // Then
            verify(telegramBot, never()).sendMessage(eq(chatId), eq(null), anyString());
        }
    }

    @Test
    public void testHandleUsernameInput() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Mocking the static method getUserCurrentBotState
            mockedBotAttributes.when(() -> BotAttributes.getUserCurrentBotState(anyLong()))
                    .thenReturn(BotState.AWAITING_USERNAME_INPUT);
            // Given
            Long userId = 123L;
            Long chatId = 456L;
            String username = "testUser";
            when(botAttributes.getUsername()).thenReturn(username);
            // Mocking the usersInProgress set behavior
            UserRegistrationService spyUserService = spy(userRegistrationService);
            spyUserService.setUsersInProgress(userId);

            // When
            spyUserService.handleUsernameInput(userId, chatId, username);

            // Then
            verify(telegramBot, times(1)).sendMessage(eq(chatId), eq(null), eq("Имя пользователя принято. Введите пароль."));
        }
    }

    @Test
    public void testHandlePasswordInput() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Mocking the static method getUserCurrentBotState
            mockedBotAttributes.when(() -> BotAttributes.getUserCurrentBotState(anyLong()))
                    .thenReturn(BotState.AWAITING_USERNAME_INPUT);
            // Given
            Long userId = 123L;
            Long chatId = 456L;
            String password = "testPassword";
            String username = "testUser";
            when(botAttributes.getUsername()).thenReturn(username);

            // Mocking the usersInProgress set behavior
            UserRegistrationService spyUserService = spy(userRegistrationService);
            spyUserService.setUsersInProgress(userId);

            // When
            UserDTO userDTO = spyUserService.handlePasswordInput(userId, chatId, password);

            // Then
            assertNotNull(userDTO);
            assertEquals(userId, userDTO.getUsertgId());
            assertEquals(username, userDTO.getUsername());
            assertEquals(password, userDTO.getPassword());
            assertEquals(UserRole.USER, userDTO.getRole());
            verify(telegramBot, times(1)).sendMessage(eq(chatId), eq(null), eq("Пароль принят."));
        }
    }

    @Test
    public void testCompleteRegistration() {
        // Given
        Long userId = 123L;
        Long chatId = 456L;

        // When
        userRegistrationService.completeRegistration(userId, chatId);

        // Then
        verify(telegramBot, times(1)).sendMessage(eq(chatId), eq(null), eq("Вы зарегистрированы. Теперь можете пользоваться командами бота"));
    }
}