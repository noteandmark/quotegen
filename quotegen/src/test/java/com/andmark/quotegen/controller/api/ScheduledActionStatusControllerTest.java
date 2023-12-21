package com.andmark.quotegen.controller.api;

import com.andmark.quotegen.controller.api.ScheduledActionStatusController;
import com.andmark.quotegen.service.SchedulledActionStatusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.andmark.quotegen.dto.ScheduledActionStatusDTO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScheduledActionStatusController.class)
class ScheduledActionStatusControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SchedulledActionStatusService scheduledActionStatusService;

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    public void testGetSchedulledActionStatus() throws Exception {
        // Mocking
        ScheduledActionStatusDTO mockStatusDTO = new ScheduledActionStatusDTO();
        when(scheduledActionStatusService.getSchedulledActionStatus()).thenReturn(mockStatusDTO);

        // Testing
        mockMvc.perform(get("/api/scheduled/random"))
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(mockStatusDTO)));
    }

    @Test
    @Disabled
    public void testUpdateSchedulledActionStatus() throws Exception {
        // Mocking
        ScheduledActionStatusDTO mockStatusDTO = new ScheduledActionStatusDTO();

        // Testing
        mockMvc.perform(post("/api/scheduled/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(mockStatusDTO)))
                .andExpect(status().isOk());

        verify(scheduledActionStatusService, times(1)).updateSchedulledActionStatus(mockStatusDTO);
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