package com.andmark.quotegen.controller.api;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.dto.BookDTO;
import com.andmark.quotegen.dto.StatsDTO;
import com.andmark.quotegen.service.BookService;
import com.andmark.quotegen.service.impl.ScanServiceImpl;
import com.andmark.quotegen.util.impl.Fb2BookFormatParser;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebMvcTest(ScanController.class)
class ScanControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ScanServiceImpl scanServiceImpl;
    @MockBean
    private BookService bookService;
    @MockBean
    private Fb2BookFormatParser parser;

    @Test
    public void testParseBook() throws Exception {
        // Mocking
        Long bookId = 1L;
        String parsedContent = "Parsed content goes here...";

        BookDTO mockBookDTO = new BookDTO();
        mockBookDTO.setId(bookId);
        Book book = new Book();
        book.setId(bookId);

        when(bookService.findOne(bookId)).thenReturn(mockBookDTO);

        ModelMapper mockMapper = mock(ModelMapper.class);
        when(mockMapper.map(mockBookDTO, Book.class)).thenReturn(book);

        when(parser.parse(book)).thenReturn(parsedContent);

        mockMvc.perform(get("/api/test-parse-fb2")
                        .param("id", String.valueOf(bookId)))
                .andExpect(status().isOk())
                .andExpect(content().string(parsedContent));
    }

    @Test
    public void testScanBooks() throws Exception {
        // Mocking
        String directoryPath = "path/to/directory";
        List<BookDTO> mockScannedBooks = new ArrayList<>();
        when(scanServiceImpl.scanBooks(directoryPath)).thenReturn(mockScannedBooks);

        // Testing
        mockMvc.perform(get("/api/scan-books")
                        .param("directoryPath", directoryPath))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(mockScannedBooks.size())));
    }

    @Test
    public void testGetStatistics() throws Exception {
        // Mocking
        StatsDTO mockStats = new StatsDTO(10L, 5L, 3L);
        when(scanServiceImpl.getStatistics()).thenReturn(mockStats);

        // Testing
        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookCount", equalTo(mockStats.getBookCount().intValue())))
                .andExpect(jsonPath("$.publishedQuotesThisYear", equalTo(mockStats.getPublishedQuotesThisYear().intValue())))
                .andExpect(jsonPath("$.pendingQuotesCount", equalTo(mockStats.getPendingQuotesCount().intValue())));
    }

    @Test
    public void testTestConnection() throws Exception {
        // Testing
        mockMvc.perform(get("/api/test-connection"))
                .andExpect(status().isOk())
                .andExpect(content().string("Connection is ok"));
    }

}