package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.domain.enums.UserRole;
import com.andmark.quotegen.dto.UserDTO;
import com.andmark.quotegen.exception.ServiceException;
import com.andmark.quotegen.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = {"USER"})
public class WebProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserServiceImpl userService;

    @Test
    void userProfile_ShouldReturnUserProfilePage_WhenUserExists() throws Exception {
        // Arrange
        String username = "testUser";
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername(username);
        userDTO.setNickname(username);
        userDTO.setRole(UserRole.ROLE_USER);

        when(userService.findByUsername(username)).thenReturn(userDTO);

        // Act & Assert
        mockMvc.perform(get("/web/profile/{username}", username))
                .andExpect(status().isOk())
                .andExpect(view().name("web/profile"))
                .andExpect(model().attribute("user", userDTO));
    }

    @Test
    void userProfile_ShouldReturnErrorPage_WhenUserDoesNotExist() throws Exception {
        // Arrange
        String username = "nonExistingUser";

        when(userService.findByUsername(username)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/web/profile/{username}", username))
                .andExpect(status().isOk())
                .andExpect(view().name("public/error"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "Пользователь с именем " + username + " не найден."));
    }

    @Test
    void shouldUpdateNicknameAndRedirectToUserProfile() throws Exception {
        // Arrange
        String username = "testuser";
        UserDTO updatedUser = createMockUser("1", "newusername");
        when(userService.updateNickname(updatedUser)).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(post("/web/profile/{username}", username)
                        .param("id", "1")
                        .param("username", "newusername"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/profile/" + username));
    }

    @Test
    void shouldHandleServiceExceptionAndRedirectToErrorPage() throws Exception {
        // Arrange
        String username = "testuser";
        String errorMessage = "Error occurred while updating user nickname";
        doThrow(new ServiceException(errorMessage)).when(userService).updateNickname(any(UserDTO.class));

        // Act & Assert
        mockMvc.perform(post("/web/profile/{username}", username)
                        .param("id", "1")
                        .param("username", "newusername"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/error"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", errorMessage));
    }

    @Test
    void shouldReturnChangePasswordForm() throws Exception {
        // Arrange
        String username = "testuser";

        // Act & Assert
        mockMvc.perform(get("/web/change-password/{username}", username))
                .andExpect(status().isOk())
                .andExpect(view().name("web/change-password"))
                .andExpect(model().attribute("username", username));
    }

    @Test
    void shouldChangePasswordSuccessfully() throws Exception {
        // Arrange
        String username = "testuser";
        String currentPassword = "oldPassword";
        String newPassword = "newPassword";

        // Mock the userService.changePassword method to do nothing (success case)
        doNothing().when(userService).changePassword(username, currentPassword, newPassword);

        // Act & Assert
        mockMvc.perform(post("/web/change-password/{username}", username)
                        .param("currentPassword", currentPassword)
                        .param("newPassword", newPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/change-password/" + username))
                .andExpect(flash().attribute("successMessage", "Password changed successfully"));
    }

    @Test
    void shouldHandleIllegalArgumentException() throws Exception {
        // Arrange
        String username = "testuser";
        String currentPassword = "oldPassword";
        String newPassword = "newPassword";
        String errorMessage = "Invalid password format";

        // Mock the userService.changePassword method to throw an IllegalArgumentException
        doThrow(new IllegalArgumentException(errorMessage))
                .when(userService).changePassword(username, currentPassword, newPassword);

        // Act & Assert
        mockMvc.perform(post("/web/change-password/{username}", username)
                        .param("currentPassword", currentPassword)
                        .param("newPassword", newPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/change-password/" + username))
                .andExpect(flash().attribute("errorMessage", errorMessage));
    }

    @Test
    void shouldDeleteUserSuccessfully() throws Exception {
        // Arrange
        String username = "testuser";

        // Mock the userService.deleteByUsername method to do nothing (success case)
        doNothing().when(userService).deleteByUsername(username);

        // Act & Assert
        mockMvc.perform(post("/web/profile/delete/{username}", username))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signout"));
    }

    @Test
    void shouldHandleServiceExceptionInDeleteUser() throws Exception {
        // Arrange
        String username = "testuser";
        String errorMessage = "Error occurred while deleting user";

        // Mock the userService.deleteByUsername method to throw a ServiceException
        doThrow(new ServiceException(errorMessage)).when(userService).deleteByUsername(username);

        // Act & Assert
        mockMvc.perform(post("/web/profile/delete/{username}", username))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/profile/" + username + "?error=" + errorMessage));
    }

    private UserDTO createMockUser(String id, String username) {
        UserDTO user = new UserDTO();
        user.setId(Long.parseLong(id));
        user.setUsername(username);
        user.setNickname(username);
        user.setRole(UserRole.ROLE_USER);
        return user;
    }
}