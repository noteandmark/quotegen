package com.andmark.qoutegen.service.impl;

import com.andmark.qoutegen.models.Book;
import com.andmark.qoutegen.models.Quote;
import com.andmark.qoutegen.models.enums.BookFormat;
import com.andmark.qoutegen.models.enums.Status;
import com.andmark.qoutegen.repository.BooksRepository;
import com.andmark.qoutegen.repository.QuotesRepository;
import com.andmark.qoutegen.util.BookFormatParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class QuoteServiceImplTest {
    @InjectMocks
    private QuoteServiceImpl quoteService;
    @Mock
    private Queue<Quote> quoteCache;
    @Mock
    private BooksRepository booksRepository;
    @Mock
    private QuotesRepository quotesRepository;
    @Mock
    private BookFormatParser bookFormatParser;

    @Test
    public void whenGetAll30DifferentBooks_thenShouldPopulateCache() {
        int cacheSize = 30; // Set the cache size
        quoteService.setCacheSize(cacheSize); // Set the cache size in the service

        List<Book> allBooks = new ArrayList<>();
        for (int i = 1; i <= cacheSize; i++) {
            Book book = new Book();
            book.setId((long) i);
            book.setTitle("Book " + i);
            book.setAuthor("Author " + i);
            book.setFormat(BookFormat.PDF);
            book.setStatus(Status.ACTIVE);
            allBooks.add(book);
        }
        when(booksRepository.findByStatus(Status.ACTIVE)).thenReturn(allBooks);
        when(bookFormatParser.parse(any())).thenReturn("This is a sample book text.");
        quoteService.populateCache();
        verify(booksRepository, times(1)).findByStatus(Status.ACTIVE);
        verify(bookFormatParser, atLeastOnce()).parse(any());
    }


}