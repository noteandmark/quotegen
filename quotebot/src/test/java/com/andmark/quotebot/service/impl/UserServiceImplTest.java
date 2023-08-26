package com.andmark.quotebot.service.impl;

import com.andmark.quotebot.service.ApiService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceImplTest {
    @Mock
    private UserRegistrationService userRegistrationService;

    @Mock
    private ApiService apiService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void testInitiateRegistration() {
        // Given
        Long userId = 123L;
        Long chatId = 456L;

        // When
        userService.initiateRegistration(userId, chatId);

        // Then
        verify(userRegistrationService, times(1)).initiateRegistration(userId, chatId);
    }

    @Test
    public void testDeleteUser() {
        // Given
        Long chatId = 456L;
        Long userId = 123L;

        // When
        userService.deleteUser(chatId, userId);

        // Then
        verify(apiService, times(1)).deleteUser(chatId, userId);
    }

    @Test
    public void testIsRegistered_True() {
        // Given
        Long userId = 123L;
        when(apiService.existsByUsertgId(userId)).thenReturn(true);

        // When
        boolean result = userService.isRegistered(userId);

        // Then
        assertTrue(result);
    }

    @Test
    public void testIsRegistered_False() {
        // Given
        Long userId = 123L;
        when(apiService.existsByUsertgId(userId)).thenReturn(false);

        // When
        boolean result = userService.isRegistered(userId);

        // Then
        assertFalse(result);
    }

    @Test
    public void testIsUsernameTaken_True() {
        // Given
        String username = "testUsername";
        when(apiService.existsByUsername(username)).thenReturn(true);

        // When
        boolean result = userService.isUsernameTaken(username);

        // Then
        assertTrue(result);
    }

    @Test
    public void testIsUsernameTaken_False() {
        // Given
        String username = "testUsername";
        when(apiService.existsByUsername(username)).thenReturn(false);

        // When
        boolean result = userService.isUsernameTaken(username);

        // Then
        assertFalse(result);
    }
}