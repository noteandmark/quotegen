package com.andmark.quotebot.service.impl;

import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.service.ApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DefaultUserRoleServiceTest {
    @Mock
    private ApiService apiService;

    @InjectMocks
    private DefaultUserRoleService userRoleService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testHasRequiredRole_AdminUserWithAdminRequired_ShouldReturnTrue() {
        // Given
        Long userId = 1L;
        UserRole userRole = UserRole.ROLE_ADMIN;
        UserRole neededRole = UserRole.ROLE_ADMIN;

        when(apiService.getUserRole(userId)).thenReturn(userRole);

        // When
        boolean result = userRoleService.hasRequiredRole(userId, neededRole);

        // Then
        assertTrue(result);
        verify(apiService, times(1)).getUserRole(userId);
    }

    @Test
    public void testHasRequiredRole_AdminUserWithUserRequired_ShouldReturnTrue() {
        // Given
        Long userId = 1L;
        UserRole userRole = UserRole.ROLE_ADMIN;
        UserRole neededRole = UserRole.ROLE_USER;

        when(apiService.getUserRole(userId)).thenReturn(userRole);

        // When
        boolean result = userRoleService.hasRequiredRole(userId, neededRole);

        // Then
        assertTrue(result);
        verify(apiService, times(1)).getUserRole(userId);
    }

    @Test
    public void testHasRequiredRole_UserWithUserRequired_ShouldReturnTrue() {
        // Given
        Long userId = 1L;
        UserRole userRole = UserRole.ROLE_USER;
        UserRole neededRole = UserRole.ROLE_USER;

        when(apiService.getUserRole(userId)).thenReturn(userRole);

        // When
        boolean result = userRoleService.hasRequiredRole(userId, neededRole);

        // Then
        assertTrue(result);
        verify(apiService, times(1)).getUserRole(userId);
    }

    @Test
    public void testHasRequiredRole_UserWithAdminRequired_ShouldReturnFalse() {
        // Given
        Long userId = 1L;
        UserRole userRole = UserRole.ROLE_USER;
        UserRole neededRole = UserRole.ROLE_ADMIN;

        when(apiService.getUserRole(userId)).thenReturn(userRole);

        // When
        boolean result = userRoleService.hasRequiredRole(userId, neededRole);

        // Then
        assertFalse(result);
        verify(apiService, times(1)).getUserRole(userId);
    }

}