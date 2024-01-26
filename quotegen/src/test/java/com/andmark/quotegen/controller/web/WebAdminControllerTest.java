package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.domain.enums.QuoteStatus;
import com.andmark.quotegen.dto.QuoteDTO;
import com.andmark.quotegen.service.GoogleCustomSearchService;
import com.andmark.quotegen.service.QuoteService;
import com.andmark.quotegen.service.WebAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = {"ADMIN"})
public class WebAdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuoteService quoteService;

    @MockBean
    private WebAdminService webAdminService;

    @MockBean
    private GoogleCustomSearchService googleCustomSearchService;

    @Test
    void shouldReturnSuccessPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/admin/success"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/success"));
    }

    @Test
    void shouldReturnRequestQuotePage() throws Exception {
        // Arrange
        QuoteDTO mockQuoteDTO = new QuoteDTO();
        mockQuoteDTO.setContent("Test quote content");

        List<String> mockImageUrls = Arrays.asList("url1", "url2", "url3");

        when(quoteService.provideQuoteToClient()).thenReturn(mockQuoteDTO);
        when(googleCustomSearchService.searchImagesByKeywords(anyString())).thenReturn(mockImageUrls);

        // Act & Assert
        mockMvc.perform(get("/admin/requestquote"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/requestquote"))
                .andExpect(model().attribute("quote", mockQuoteDTO))
                .andExpect(model().attribute("imageUrls", mockImageUrls))
                .andExpect(model().attribute("selectedImageNumber", 0));
    }

    @Test
    void shouldAcceptQuote() throws Exception {
        // Arrange
        QuoteDTO pendingQuote = new QuoteDTO();
        pendingQuote.setId(1L);
        pendingQuote.setContent("Test quote content");
        pendingQuote.setStatus(QuoteStatus.PENDING);

        String publishOption = "random";
        String publishDate = "2024-01-24T12:00:00";
        String selectedImageUrl = "http://example.com/image.jpg";

        // Act & Assert
        mockMvc.perform(post("/admin/acceptquote")
                        .flashAttr("quote", pendingQuote)
                        .param("publishOption", publishOption)
                        .param("publishDate", publishDate)
                        .param("selectedImageUrl", selectedImageUrl))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/success"))
                .andExpect(flash().attribute("confirmation", true))
                .andExpect(flash().attribute("quote", pendingQuote));

        // Verify that the service methods were called with the expected parameters
        verify(webAdminService, times(1)).randomPublish(pendingQuote);
    }

    @Test
    void shouldRejectQuote() throws Exception {
        // Arrange
        long quoteId = 1L;

        // Act & Assert
        mockMvc.perform(post("/admin/rejectquote")
                        .param("quoteId", String.valueOf(quoteId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/success"))
                .andExpect(flash().attribute("deletion", true))
                .andExpect(flash().attribute("deletedQuoteId", quoteId));

        // Verify that the service method was called with the expected parameter
        verify(quoteService, times(1)).rejectQuote(quoteId);
    }

}