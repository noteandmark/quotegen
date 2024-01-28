package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.UserDTO;
import com.andmark.quotegen.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = {"ADMIN"})
public class WebAdminUsersControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserServiceImpl userService;

    @Test
    void shouldShowUsers() throws Exception {
        // Arrange
        List<UserDTO> mockUsers = Arrays.asList(
                createMockUser("1", "John"),
                createMockUser("2", "Alice")
        );
        when(userService.findAll()).thenReturn(mockUsers);

        // Act & Assert
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/list"))
                .andExpect(model().attribute("users", mockUsers));
    }

    @Test
    void shouldViewUser() throws Exception {
        // Arrange
        Long userId = 1L;
        UserDTO userDTO = createMockUser("1", "John");

        when(userService.findOne(userId)).thenReturn(userDTO);

        // Act & Assert
        mockMvc.perform(get("/admin/users/view/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/view"))
                .andExpect(model().attribute("userDTO", userDTO));
    }

    @Test
    void shouldShowEditForm() throws Exception {
        // Arrange
        Long userId = 1L;
        UserDTO userDTO = createMockUser("1", "testuser");

        when(userService.findOne(userId)).thenReturn(userDTO);

        // Act & Assert
        mockMvc.perform(get("/admin/users/edit/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/edit"))
                .andExpect(model().attribute("userDTO", userDTO));
    }

    @Test
    void shouldEditUser() throws Exception {
        // Arrange
        Long userId = 1L;
        UserDTO userDTO = createMockUser("1", "testuser");

        when(userService.findOne(userId)).thenReturn(userDTO);

        // Act & Assert
        mockMvc.perform(post("/admin/users/edit/{id}", userId)
                        .param("id", userId.toString())
                        .param("username", "newUsername")
                        .param("password", "newPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService).update(any(UserDTO.class));
    }

    private UserDTO createMockUser(String id, String username) {
        UserDTO user = new UserDTO();
        user.setId(Long.parseLong(id));
        user.setUsername(username);
        return user;
    }

}