package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.BookDTO;
import com.andmark.quotegen.dto.QuoteDTO;
import com.andmark.quotegen.service.BookService;
import com.andmark.quotegen.service.QuoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
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
public class WebAdminQuoteControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private QuoteService quoteService;
    @MockBean
    private BookService bookService;

    @Test
    void shouldShowQuotes() throws Exception {
        // Arrange
        QuoteDTO quoteDTO1 = new QuoteDTO();
        quoteDTO1.setId(1L);
        quoteDTO1.setContent("Quote 1");

        QuoteDTO quoteDTO2 = new QuoteDTO();
        quoteDTO2.setId(2L);
        quoteDTO2.setContent("Quote 2");

        List<QuoteDTO> quotes = Arrays.asList(
                quoteDTO1, quoteDTO2
        );
        Page<QuoteDTO> quotesPage = new PageImpl<>(quotes);
        System.out.println("test quotesPage = " + quotesPage);

        when(quoteService.findAllSorted(
                any(Pageable.class),
                eq("expectedSortField"),
                eq("expectedSortDirection")
        )).thenReturn(quotesPage);

        // Создаем Pageable с ожидаемыми параметрами сортировки
        Pageable pageable = PageRequest.of(0, 20, Sort.unsorted());

        // Act & Assert
        mockMvc.perform(get("/admin/quote")
                        .param("sortField", "expectedSortField")
                        .param("sortDirection", "expectedSortDirection"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/quote/list"))
                .andExpect(model().attribute("quotes", quotesPage.getContent()))
                .andExpect(model().attribute("page", quotesPage))
                .andExpect(model().attribute("sortField", "expectedSortField"))
                .andExpect(model().attribute("sortDirection", "expectedSortDirection"));

        verify(quoteService).findAllSorted(pageable, "expectedSortField", "expectedSortDirection");
    }

    @Test
    public void shouldViewQuote() throws Exception {
        // Arrange
        Long quoteId = 1L;
        Long bookId = 2L;
        QuoteDTO quoteDTO = new QuoteDTO();
        quoteDTO.setId(quoteId);
        quoteDTO.setBookId(bookId);

        BookDTO bookDTO = new BookDTO();
        bookDTO.setId(bookId);
        bookDTO.setTitle("Test Book");

        when(quoteService.findOne(quoteId)).thenReturn(quoteDTO);
        when(bookService.findOne(bookId)).thenReturn(bookDTO);

        // Act & Assert
        mockMvc.perform(get("/admin/quote/view/{id}", quoteId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("quoteDTO", quoteDTO))
                .andExpect(model().attribute("bookDTO", bookDTO))
                .andExpect(view().name("admin/quote/view"));
    }

    @Test
    void shouldRedirectIfQuoteNotFound() throws Exception {
        // Arrange
        Long quoteId = 1L;
        when(quoteService.findOne(quoteId)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/admin/quote/view/{id}", quoteId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/quote"))
                .andExpect(view().name("redirect:/admin/quote"));
    }


    @Test
    public void shouldShowCreateForm() throws Exception {
        // Arrange
        BookDTO bookDTO1 = new BookDTO();
        bookDTO1.setId(1L);
        bookDTO1.setTitle("Book 1");

        BookDTO bookDTO2 = new BookDTO();
        bookDTO2.setId(1L);
        bookDTO2.setTitle("Book 2");

        List<BookDTO> mockBookList = Arrays.asList(
                bookDTO1,
                bookDTO2
        );
        when(bookService.findAll()).thenReturn(mockBookList);

        // Act & Assert
        mockMvc.perform(get("/admin/quote/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/quote/create"))
                .andExpect(model().attributeExists("quoteDTO"))
                .andExpect(model().attribute("bookList", mockBookList));
    }

    @Test
    public void shouldCreateQuote() throws Exception {
        // Arrange
        QuoteDTO quoteDTO = new QuoteDTO();
        quoteDTO.setContent("Test quote content");

        // Act & Assert
        mockMvc.perform(post("/admin/quote/create")
                        .flashAttr("quoteDTO", quoteDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/quote"));

        verify(quoteService).save(quoteDTO);
    }

    @Test
    void shouldShowEditForm() throws Exception {
        // Arrange
        long quoteId = 1L;
        long bookId = 2L;

        QuoteDTO mockQuoteDTO = new QuoteDTO();
        mockQuoteDTO.setId(quoteId);
        mockQuoteDTO.setBookId(bookId);
        mockQuoteDTO.setContent("Test content");

        BookDTO mockBookDTO = new BookDTO();
        mockBookDTO.setId(bookId);
        mockBookDTO.setTitle("Test book");

        when(quoteService.findOne(quoteId)).thenReturn(mockQuoteDTO);
        when(bookService.findOne(bookId)).thenReturn(mockBookDTO);

        // Act & Assert
        mockMvc.perform(get("/admin/quote/edit/{id}", quoteId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/quote/edit"))
                .andExpect(model().attribute("quoteDTO", mockQuoteDTO))
                .andExpect(model().attribute("bookDTO", mockBookDTO));

        verify(quoteService).findOne(quoteId);
        verify(bookService).findOne(bookId);
    }

    @Test
    void shouldEditQuote() throws Exception {
        // Arrange
        Long quoteId = 1L;
        QuoteDTO mockQuote = new QuoteDTO();
        mockQuote.setId(quoteId);
        mockQuote.setContent("Updated content");

        // Act & Assert
        mockMvc.perform(post("/admin/quote/edit/{id}", quoteId)
                        .param("id", String.valueOf(quoteId))
                        .param("content", "Updated content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/quote"));

        verify(quoteService).update(mockQuote);
    }

    @Test
    void shouldDeleteQuote() throws Exception {
        // Arrange
        Long quoteId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/admin/quote/delete/{id}", quoteId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/quote"));

        verify(quoteService).delete(quoteId);
    }

}