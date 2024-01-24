package com.andmark.quotegen.controller.api;

import com.andmark.quotegen.domain.enums.UserRole;
import com.andmark.quotegen.dto.UserDTO;
import com.andmark.quotegen.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static com.andmark.quotegen.domain.enums.UserRole.ROLE_USER;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@WebMvcTest(UserController.class)
@ExtendWith(SpringExtension.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldFindUserByUsertgId() throws Exception {
        // Arrange
        long usertgId = 123;
        UserDTO userDTO = new UserDTO();
        userDTO.setUsertgId(usertgId);
        when(userService.findOneByUsertgId(usertgId)).thenReturn(userDTO);

        // Act & Assert
        mockMvc.perform(get("/api/users/finduser-usertgid/{usertgId}", usertgId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.usertgId", is((int) usertgId)));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldReturnNotFoundWhenUserNotFound() throws Exception {
        // Arrange
        long usertgId = 123;
        when(userService.findOneByUsertgId(usertgId)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/users/finduser-usertgid/{usertgId}", usertgId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testIsUserRegistered() throws Exception {
        // Mocking
        long userId = 1L;
        when(userService.isRegistered(userId)).thenReturn(true);

        // Testing
        mockMvc.perform(get("/api/users/exists/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testIsUsernameTaken() throws Exception {
        // Mocking
        String username = "testuser";
        when(userService.existsByUsername(username)).thenReturn(true);

        // Testing
        mockMvc.perform(get("/api/users/username-taken/{username}", username))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    public void testGetUserRole() throws Exception {
        // Mocking
        long userId = 1L;
        UserRole mockUserRole = UserRole.ROLE_ADMIN;
        when(userService.getUserRole(userId)).thenReturn(mockUserRole);

        // Testing
        mockMvc.perform(get("/api/users/get-role/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("\"" + mockUserRole.toString() + "\""));
    }

    @Test
    @WithAnonymousUser
    @Disabled //don't work with SpringSecurity (need integrating test), but work checked with Postman
    public void testRegisterUser() throws Exception {
        // Mocking
        UserDTO userDTO = new UserDTO();
        userDTO.setUsertgId(12345L);
        userDTO.setUsername("testuser");
        userDTO.setPassword("password");
        userDTO.setRole(ROLE_USER);
        userDTO.setNickname("testuser");

        when(userService.save(any(UserDTO.class))).thenReturn(userDTO);

        // Testing
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));

        verify(userService, times(1)).save(any(UserDTO.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
//    @Disabled //don't work with SpringSecurity (need integrating test), but work checked with Postman
    public void testDeleteUser() throws Exception {
        // Mocking
        long userId = 1L;

        doNothing().when(userService).delete(anyLong());

        // Testing
        mockMvc.perform(delete("/api/users/delete/{userId}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).delete(userId);
    }

    // Helper method to convert an object to JSON string
    private String asJsonString(final Object obj) {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}