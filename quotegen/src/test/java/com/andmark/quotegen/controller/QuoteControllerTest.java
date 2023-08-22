package com.andmark.quotegen.controller;

import com.andmark.quotegen.dto.QuoteDTO;
import com.andmark.quotegen.exception.NotFoundBookException;
import com.andmark.quotegen.service.QuoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class QuoteControllerTest {

    @Mock
    private QuoteService quoteService;

    @InjectMocks
    private QuoteController quoteController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllQuotes() {
        List<QuoteDTO> expectedQuotes = new ArrayList<>();

        when(quoteService.findAll()).thenReturn(expectedQuotes);

        ResponseEntity<List<QuoteDTO>> responseEntity = quoteController.getAllQuotes();

        verify(quoteService, times(1)).findAll();
        assertEquals(ResponseEntity.ok(expectedQuotes), responseEntity);
    }

    @Test
    void testGetQuoteByIdExisting() {
        Long quoteId = 1L;
        QuoteDTO expectedQuote = new QuoteDTO();
        when(quoteService.findOne(quoteId)).thenReturn(expectedQuote);

        ResponseEntity<QuoteDTO> responseEntity = quoteController.getQuoteById(quoteId);

        verify(quoteService, times(1)).findOne(quoteId);
        assertEquals(ResponseEntity.ok(expectedQuote), responseEntity);
    }

    @Test
    void testGetQuoteByIdNonExisting() {
        Long quoteId = 1L;
        when(quoteService.findOne(quoteId)).thenReturn(null);

        ResponseEntity<QuoteDTO> responseEntity = quoteController.getQuoteById(quoteId);

        verify(quoteService, times(1)).findOne(quoteId);
        assertEquals(ResponseEntity.notFound().build(), responseEntity);
    }

    @Test
    void testDeleteQuoteExisting() {
        Long quoteId = 1L;
        QuoteDTO existingQuote = new QuoteDTO();
        when(quoteService.findOne(quoteId)).thenReturn(existingQuote);

        ResponseEntity<Void> responseEntity = quoteController.deleteQuote(quoteId);

        verify(quoteService, times(1)).delete(quoteId);
        assertEquals(ResponseEntity.ok().build(), responseEntity);
    }

    @Test
    void testDeleteQuoteNonExisting() {
        Long quoteId = 1L;
        when(quoteService.findOne(quoteId)).thenReturn(null);

        ResponseEntity<Void> responseEntity = quoteController.deleteQuote(quoteId);

        verify(quoteService, never()).delete(quoteId);
        assertEquals(ResponseEntity.notFound().build(), responseEntity);
    }

    @Test
    void testGetNextQuoteWhenNoBooksAvailable() throws NotFoundBookException {
        when(quoteService.provideQuoteToClient()).thenThrow(NotFoundBookException.class);

        ResponseEntity<QuoteDTO> responseEntity = quoteController.getNextQuote();

        verify(quoteService, times(1)).checkAndPopulateCache();
        verify(quoteService, times(1)).provideQuoteToClient();
        assertEquals(ResponseEntity.ok(QuoteDTO.createErrorMessage("No books available. Please scan the catalogue first.")), responseEntity);
    }

    @Test
    void testGetPendingQuotes() {
        List<QuoteDTO> expectedQuotes = new ArrayList<>();
        when(quoteService.getPendingQuotes()).thenReturn(expectedQuotes);

        ResponseEntity<List<QuoteDTO>> responseEntity = quoteController.getPendingQuotes();

        verify(quoteService, times(1)).getPendingQuotes();
        assertEquals(ResponseEntity.ok(expectedQuotes), responseEntity);
    }

    @Test
    void testPendingQuote() {
        QuoteDTO quoteDTO = new QuoteDTO();
        ResponseEntity<Void> responseEntity = quoteController.pendingQuote(quoteDTO);

        verify(quoteService, times(1)).pendingQuote(quoteDTO);
        assertEquals(ResponseEntity.ok().build(), responseEntity);
    }

    @Test
    void testConfirmQuote() {
        QuoteDTO quoteDTO = new QuoteDTO();
        ResponseEntity<Void> responseEntity = quoteController.confirmQuote(quoteDTO);

        verify(quoteService, times(1)).confirmQuote(quoteDTO);
        assertEquals(ResponseEntity.ok().build(), responseEntity);
    }

    @Test
    void testRejectQuoteExisting() {
        Long quoteId = 1L;
        QuoteDTO existingQuote = new QuoteDTO();
        when(quoteService.findOne(quoteId)).thenReturn(existingQuote);

        ResponseEntity<Void> responseEntity = quoteController.rejectQuote(quoteId);

        verify(quoteService, times(1)).delete(quoteId);
        assertEquals(ResponseEntity.ok().build(), responseEntity);
    }

    @Test
    void testRejectQuoteNonExisting() {
        Long quoteId = 1L;
        when(quoteService.findOne(quoteId)).thenReturn(null);

        ResponseEntity<Void> responseEntity = quoteController.rejectQuote(quoteId);

        verify(quoteService, never()).delete(quoteId);
        assertEquals(ResponseEntity.notFound().build(), responseEntity);
    }

    @Test
    void testGenerateQuotesWithCount() {
        int count = 10;
        ResponseEntity<Void> responseEntity = quoteController.generateQuotes(count);

        verify(quoteService, times(1)).populateCache(count);
        assertEquals(ResponseEntity.ok().build(), responseEntity);
    }

    @Test
    void testGenerateQuotesWithoutCount() {
        ResponseEntity<Void> responseEntity = quoteController.generateQuotes(null);

        verify(quoteService, times(1)).populateCache(30); // Default cache size
        assertEquals(ResponseEntity.ok().build(), responseEntity);
    }

    @Test
    void testGetRandomPublishedQuote() {
        QuoteDTO expectedQuote = new QuoteDTO();
        when(quoteService.getRandomPublishedQuote()).thenReturn(expectedQuote);

        ResponseEntity<QuoteDTO> responseEntity = quoteController.getRandomPublishedQuote();

        verify(quoteService, times(1)).getRandomPublishedQuote();
        assertEquals(ResponseEntity.ok(expectedQuote), responseEntity);
    }

    @Test
    void testGetPublishedQuotesForWeek() {
        List<QuoteDTO> expectedQuotes = new ArrayList<>();
        when(quoteService.getPublishedQuotesForWeek()).thenReturn(expectedQuotes);

        ResponseEntity<List<QuoteDTO>> responseEntity = quoteController.getPublishedQuotesForWeek();

        verify(quoteService, times(1)).getPublishedQuotesForWeek();
        assertEquals(ResponseEntity.ok(expectedQuotes), responseEntity);
    }

}