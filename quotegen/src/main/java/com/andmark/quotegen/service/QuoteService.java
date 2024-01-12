package com.andmark.quotegen.service;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.dto.AvailableDayResponseDTO;
import com.andmark.quotegen.dto.QuoteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuoteService extends AbstractService<QuoteDTO>{
    void checkAndPopulateCache();
    QuoteDTO provideQuoteToClient();
    void confirmQuote(QuoteDTO quoteDTO);
    void rejectQuote(Long id);
    void populateCache(Integer cacheSize);
    List<QuoteDTO> getPendingQuotes();
    void pendingQuote(QuoteDTO quoteDTO);

    QuoteDTO getRandomPublishedQuote();

    List<QuoteDTO> getPublishedQuotesForWeek();

    String getBookText(Book book);

    AvailableDayResponseDTO getAvailableDays();

    Page<QuoteDTO> findAllSorted(Pageable pageable, String sortField, String sortDirection);

    void suggestQuote(QuoteDTO quoteDTO, String username);

    void addSuggestedQuote(QuoteDTO quoteDTO);
}
