package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.GreetingDTO;
import com.andmark.quotegen.service.GreetingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = {"ADMIN"})
public class WebAdminGreetingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GreetingService greetingService;

    @Test
    void shouldReturnGreetingsPage() throws Exception {
        // Arrange
        GreetingDTO greetingDTO1 = new GreetingDTO();
        greetingDTO1.setId(1L);
        greetingDTO1.setMessage("Hello");

        GreetingDTO greetingDTO2 = new GreetingDTO();
        greetingDTO2.setId(2L);
        greetingDTO2.setMessage("Hi");

        List<GreetingDTO> mockGreetings = Arrays.asList(
                greetingDTO1, greetingDTO2
        );

        when(greetingService.findAll()).thenReturn(mockGreetings);

        // Act & Assert
        mockMvc.perform(get("/admin/greeting"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/greeting/list"))
                .andExpect(model().attribute("greetings", mockGreetings));
    }

    @Test
    void shouldReturnGreetingViewPage() throws Exception {
        // Arrange
        Long greetingId = 1L;
        GreetingDTO mockGreeting = new GreetingDTO();
        mockGreeting.setId(greetingId);
        mockGreeting.setMessage("Hello");

        when(greetingService.findOne(greetingId)).thenReturn(mockGreeting);

        // Act & Assert
        mockMvc.perform(get("/admin/greeting/view/{id}", greetingId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/greeting/view"))
                .andExpect(model().attribute("greeting", mockGreeting));
    }

    @Test
    void shouldRedirectToGreetingPageWhenGreetingNotFound() throws Exception {
        // Arrange
        Long nonExistentGreetingId = 100L;
        when(greetingService.findOne(nonExistentGreetingId)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/admin/greeting/view/{id}", nonExistentGreetingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/greeting"));
    }

    @Test
    void shouldShowCreateForm() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/admin/greeting/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/greeting/create"))
                .andExpect(model().attributeExists("greeting"));
    }

    @Test
    void shouldCreateGreeting() throws Exception {
        // Arrange
        GreetingDTO greetingDTO = new GreetingDTO();
        greetingDTO.setMessage("Hello, world!");

        // Act & Assert
        mockMvc.perform(post("/admin/greeting/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("message", "Hello, world!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/greeting"));
        verify(greetingService, times(1)).save(any(GreetingDTO.class));
    }

    @Test
    void shouldShowEditForm() throws Exception {
        // Arrange
        Long greetingId = 1L;
        GreetingDTO mockGreeting = new GreetingDTO();
        mockGreeting.setId(greetingId);
        mockGreeting.setMessage("Hello");

        when(greetingService.findOne(greetingId)).thenReturn(mockGreeting);

        // Act & Assert
        mockMvc.perform(get("/admin/greeting/edit/{id}", greetingId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/greeting/edit"))
                .andExpect(model().attributeExists("greeting"))
                .andExpect(model().attribute("greeting", mockGreeting));
    }

    @Test
    void shouldEditGreeting() throws Exception {
        // Arrange
        Long greetingId = 1L;
        GreetingDTO mockGreeting = new GreetingDTO();
        mockGreeting.setId(greetingId);
        mockGreeting.setMessage("Updated message");

        // Act & Assert
        mockMvc.perform(post("/admin/greeting/edit/{id}", greetingId)
                        .param("id", String.valueOf(greetingId))
                        .param("message", "Updated message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/greeting"));

        verify(greetingService).update(argThat(greeting -> greeting.getId().equals(greetingId) && greeting.getMessage().equals("Updated message")));
    }

    @Test
    void shouldDeleteGreeting() throws Exception {
        // Arrange
        Long greetingId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/admin/greeting/delete/{id}", greetingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/greeting"));

        verify(greetingService).delete(greetingId);
    }

}