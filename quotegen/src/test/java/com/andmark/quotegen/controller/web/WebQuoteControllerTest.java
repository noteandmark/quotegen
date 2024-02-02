package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.QuoteDTO;
import com.andmark.quotegen.service.QuoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = {"USER"})
public class WebQuoteControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuoteService quoteService;

    @Test
    void shouldGetRandomQuote() throws Exception {
        // Arrange
        QuoteDTO mockQuote = new QuoteDTO();
        mockQuote.setId(1L);
        mockQuote.setContent("Test quote content");

        when(quoteService.getRandomPublishedQuote()).thenReturn(mockQuote);

        // Act & Assert
        mockMvc.perform(get("/web/getquote"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/quote"))
                .andExpect(model().attributeExists("quote"))
                .andExpect(model().attribute("quote", mockQuote));
    }

    @Test
    void shouldHandleNoPublishedQuotes() throws Exception {
        // Arrange
        when(quoteService.getRandomPublishedQuote()).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/web/getquote"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/quote"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "К сожалению, нет опубликованных цитат."));
    }

    @Test
    void shouldGetQuotesForWeek() throws Exception {
        // Arrange
        QuoteDTO quoteDTO1 = new QuoteDTO();
        quoteDTO1.setId(1L);
        quoteDTO1.setContent("Quote 1");

        QuoteDTO quoteDTO2 = new QuoteDTO();
        quoteDTO2.setId(2L);
        quoteDTO2.setContent("Quote 2");

        List<QuoteDTO> mockQuotes = Arrays.asList(
                quoteDTO1, quoteDTO2
        );

        when(quoteService.getPublishedQuotesForWeek()).thenReturn(mockQuotes);

        // Act & Assert
        mockMvc.perform(get("/web/quotes-for-week"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/quotes-for-week"))
                .andExpect(model().attributeExists("quotes"))
                .andExpect(model().attribute("quotes", mockQuotes));
    }

    @Test
    void shouldHandleNoQuotesForWeek() throws Exception {
        // Arrange
        when(quoteService.getPublishedQuotesForWeek()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/web/quotes-for-week"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/quotes-for-week"))
                .andExpect(model().attributeExists("quotes"))
                .andExpect(model().attribute("quotes", Collections.emptyList()));
    }

    @Test
    void shouldShowSuggestQuotePage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/web/suggest-quote"))
                .andExpect(status().isOk())
                .andExpect(view().name("web/suggest-quote"))
                .andExpect(model().attributeExists("quoteForm"));
    }

    @Test
    void shouldSuggestQuoteSuccessfully() throws Exception {
        // Arrange
        QuoteDTO quoteDTO = new QuoteDTO();
        quoteDTO.setContent("Test content");
        quoteDTO.setBookTitle("Test quote text");
        quoteDTO.setBookAuthor("Test author");

        doNothing().when(quoteService).suggestQuote(any(QuoteDTO.class), any(String.class));

        // Act & Assert
        mockMvc.perform(post("/web/suggest-quote")
                        .flashAttr("quoteForm", quoteDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("public/report-success"))
                .andExpect(model().attributeExists("successMessage"));
    }

    @Test
    void shouldHandleValidationErrorsInSuggestQuote() throws Exception {
        // Arrange
        QuoteDTO invalidQuoteDTO = new QuoteDTO();
        invalidQuoteDTO.setContent("");
        invalidQuoteDTO.setBookTitle("Test quote text");
        invalidQuoteDTO.setBookAuthor("Test author");

        // Act & Assert
        mockMvc.perform(post("/web/suggest-quote")
                        .flashAttr("quoteForm", invalidQuoteDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("web/suggest-quote"))
                .andExpect(model().attributeExists("org.springframework.validation.BindingResult.quoteForm"))
                .andExpect(model().attributeExists("quoteForm"));
    }

}