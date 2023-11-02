package com.andmark.quotegen.controller.api;

import com.andmark.quotegen.controller.api.BookController;
import com.andmark.quotegen.dto.ExtractedLinesDTO;
import com.andmark.quotegen.dto.PageLineRequestDTO;
import com.andmark.quotegen.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookControllerTest {
    @InjectMocks
    private BookController bookController;
    @Mock
    private BookService bookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testClearDeletedBooks() {
        ResponseEntity<String> expectedResponse = ResponseEntity.ok("Books with status DELETED cleared");
        ResponseEntity<String> actualResponse = bookController.clearDeletedBooks();

        verify(bookService, times(1)).clearDeletedBooks();
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void testProcessPageAndLine() {
        PageLineRequestDTO requestDTO = new PageLineRequestDTO();
        ExtractedLinesDTO extractedLinesDTO = new ExtractedLinesDTO();

        when(bookService.processPageAndLineNumber(requestDTO)).thenReturn(extractedLinesDTO);

        ResponseEntity<ExtractedLinesDTO> responseEntity = bookController.processPageAndLine(requestDTO);

        verify(bookService, times(1)).processPageAndLineNumber(requestDTO);
        assertEquals(ResponseEntity.ok(extractedLinesDTO), responseEntity);
    }
}