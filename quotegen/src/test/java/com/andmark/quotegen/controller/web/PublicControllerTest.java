package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.StatsDTO;
import com.andmark.quotegen.service.EmailService;
import com.andmark.quotegen.service.ScanService;
import com.andmark.quotegen.service.WebVersionService;
import com.andmark.quotegen.util.TimeProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PublicControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ScanService scanService;
    @MockBean
    private WebVersionService versionService;
    @MockBean
    private EmailService emailService;
    @MockBean
    private TimeProvider timeProvider;

    @Test
    void shouldReturnStatsPage() throws Exception {
        // Arrange
        StatsDTO statsDTO = new StatsDTO();
        statsDTO.setBookCount(10L);
        statsDTO.setPublishedQuotesThisYear(100L);
        statsDTO.setPendingQuotesCount(20L);

        when(scanService.getStatistics()).thenReturn(statsDTO);

        // Act & Assert
        mockMvc.perform(get("/public/stats"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/stats"))
                .andExpect(model().attribute("bookCount", 10L))
                .andExpect(model().attribute("publishedQuotesThisYear", 100L))
                .andExpect(model().attribute("pendingQuotesCount", 20L));
    }

    @Test
    void shouldReturnHelpPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/public/help"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/help"));
    }

    @Test
    void shouldReturnYesOrNoPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/public/da-net"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/da-net"));
    }

    @Test
    void shouldReturnVersionPage() throws Exception {
        // Arrange
        String readmeContent = "Sample readme content";
        String changelogContent = "Sample changelog content";
        String privatePolicyContent = "Sample private policy content";

        when(versionService.getReadmeContent()).thenReturn(readmeContent);
        when(versionService.getChangelogContent()).thenReturn(changelogContent);
        when(versionService.getPrivatePolicyContent()).thenReturn(privatePolicyContent);

        // Act & Assert
        mockMvc.perform(get("/public/version"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/version"))
                .andExpect(model().attribute("readmeContent", readmeContent))
                .andExpect(model().attribute("changelogContent", changelogContent))
                .andExpect(model().attribute("privatePolicyContent", privatePolicyContent));
    }

    @Test
    void shouldReturnReportPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/public/report"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/report"));
    }

    @Test
    void shouldSubmitReportFormSuccessfully() throws Exception {
        // Arrange
        String email = "test@example.com";
        String subject = "Test subject";
        String message = "Test message";

        LocalDateTime timeStampDateTime = LocalDateTime.of(2024, 1, 24, 11, 59);
        long timeStampDateTimeValue = timeStampDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String timeStamp = String.valueOf(timeStampDateTimeValue);

        LocalDateTime fixedDateTime = LocalDateTime.of(2024, 1, 24, 12, 0);
        long fixedTimeMillis = fixedDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        // mock TimeProvider.getCurrentTimeMillis() to return a fixed time value
        when(timeProvider.getCurrentTimeMillis()).thenReturn(fixedTimeMillis);

        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/public/report")
                        .param("email", email)
                        .param("subject", subject)
                        .param("message", message)
                        .param("timeStamp", timeStamp))
                .andExpect(status().isOk())
                .andExpect(view().name("public/report-success"))
                .andExpect(model().attribute("successMessage", "Ваше сообщение было успешно отправлено."));
    }

    @Test
    void shouldRejectReportFormSubmission() throws Exception {
        // Arrange
        String email = "test@example.com";
        String subject = "Test subject";
        String message = "Test message";
        String timeStamp = String.valueOf(System.currentTimeMillis());

        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/public/report")
                        .param("email", email)
                        .param("subject", subject)
                        .param("message", message)
                        .param("timeStamp", timeStamp))
                .andExpect(status().isOk())
                .andExpect(view().name("public/report-error"))
                .andExpect(model().attribute("errorMessage", "Форма отправлена слишком быстро. Возможна активность бота."));
    }

}