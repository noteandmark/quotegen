package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.domain.Quote;
import com.andmark.quotegen.domain.enums.BookFormat;
import com.andmark.quotegen.domain.enums.BookStatus;
import com.andmark.quotegen.domain.enums.QuoteStatus;
import com.andmark.quotegen.exception.NotFoundBookException;
import com.andmark.quotegen.exception.ServiceException;
import com.andmark.quotegen.repository.BooksRepository;
import com.andmark.quotegen.repository.QuotesRepository;
import com.andmark.quotegen.util.BookFormatParser;
import com.andmark.quotegen.util.BookFormatParserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    @Mock
    private BookFormatParserFactory bookFormatParserFactory;
    @Mock
    private BookFormatParser pdfBookFormatParser;
    @Mock
    private BookFormatParser docBookFormatParser;

    private int cacheSize;


    @BeforeEach
    public void setUp() {
        cacheSize = 30; // Set the cache size
        quoteService.setCacheSize(cacheSize);// Set the cache size in the service
    }

    @Test
    public void whenGetAll30DifferentBooks_thenShouldPopulateCache() {

        List<Book> allBooks = new ArrayList<>();
        for (int i = 1; i <= cacheSize; i++) {
            Book book = new Book();
            book.setId((long) i);
            book.setTitle("Book " + i);
            book.setAuthor("Author " + i);
            book.setFormat(BookFormat.PDF);
            book.setBookStatus(BookStatus.ACTIVE);
            allBooks.add(book);
        }

        when(bookFormatParserFactory.createParser(BookFormat.PDF)).thenReturn(bookFormatParser);
        when(bookFormatParser.parse(any())).thenReturn("PDF Book Content");
        when(booksRepository.findByBookStatus(BookStatus.ACTIVE)).thenReturn(allBooks);
        when(booksRepository.count()).thenReturn((long) cacheSize);
        quoteService.populateCache(cacheSize);

        verify(booksRepository, times(1)).findByBookStatus(BookStatus.ACTIVE);
        //there can only be one book in the end
        verify(bookFormatParserFactory, atLeastOnce()).createParser(BookFormat.PDF);
        verify(bookFormatParser, atLeastOnce()).parse(any());
    }

    @Test
    public void testGetBookTextForPDFBook() {
        // Arrange
        Book book = new Book();
        book.setFormat(BookFormat.PDF);

        when(bookFormatParserFactory.createParser(BookFormat.PDF)).thenReturn(bookFormatParser);
        when(bookFormatParser.parse(book)).thenReturn("PDF Book Content");

        // Act
        String bookText = quoteService.getBookText(book);

        // Assert
        assertEquals("PDF Book Content", bookText);
        verify(bookFormatParserFactory, times(1)).createParser(BookFormat.PDF);
        verify(bookFormatParser, times(1)).parse(book);
    }

    @Test
    public void testGetBookTextForDocBook() {
        // Arrange
        Book book = new Book();
        book.setFormat(BookFormat.DOC);

        when(bookFormatParserFactory.createParser(BookFormat.DOC)).thenReturn(bookFormatParser);
        when(bookFormatParser.parse(book)).thenReturn("Doc Book Content");

        // Act
        String bookText = quoteService.getBookText(book);

        // Assert
        assertEquals("Doc Book Content", bookText);
        verify(bookFormatParserFactory, times(1)).createParser(BookFormat.DOC);
        verify(bookFormatParser, times(1)).parse(book);
    }

    @Test
    public void whenWordsSizeLessThan500_thenShouldReturnWholeText() {
        List<String> words = Arrays.asList("This", "is", "a", "sample", "book", "text");
        String generatedQuote = quoteService.generateQuoteContent(words);

        String expectedQuote = "This is a sample book text";
        assertEquals(expectedQuote, generatedQuote);
    }

    @Test
    public void whenWordsSizeGreaterThan500_thenShouldReturnSublist() {
        // Create a list of words with size greater than 500
        List<String> words = new ArrayList<>();
        for (int i = 0; i < 600; i++) {
            words.add("Word" + i);
        }

        String generatedQuote = quoteService.generateQuoteContent(words);

        // Ensure that generated quote content is a sublist of the words list
        assertTrue(words.containsAll(Arrays.asList(generatedQuote.split("\\s+"))));
    }

    @Test
    public void whenNoActiveBooks_thenShouldThrowException() {
        assertThrows(NotFoundBookException.class, () -> quoteService.populateCache(cacheSize));
    }

    @Test
    public void whenCacheSizeIsZero_thenShouldNotPopulateCache() {
        quoteService.setCacheSize(0);

        try {
            when(booksRepository.count()).thenReturn(1L);
            quoteService.populateCache(cacheSize);
        } catch (ServiceException ex) {
            // Handle the exception, e.g., log it or perform assertions
            when(booksRepository.count()).thenReturn(1L);
            assertThrows(ServiceException.class, () -> quoteService.populateCache(cacheSize));
        }

        verify(quoteCache, never()).offer(any());
    }

    @Test
    public void whenDifferentBookFormats_thenShouldUseCorrectParsers() {
        List<Book> allBooks = new ArrayList<>();
        Book pdfBook = new Book(1L, "PDF Book", "Author", BookFormat.PDF, "Some folder", BookStatus.ACTIVE, null);
        Book docBook = new Book(2L, "DOC Book", "Author", BookFormat.DOC, "Some folder", BookStatus.ACTIVE, null);
        allBooks.add(pdfBook);
        allBooks.add(docBook);

        // Set up parser mocks in the factory
        when(bookFormatParserFactory.createParser(eq(BookFormat.PDF))).thenReturn(pdfBookFormatParser);
        when(bookFormatParserFactory.createParser(eq(BookFormat.DOC))).thenReturn(docBookFormatParser);
        // Set up parser interactions for pdfBookFormatParser
        when(pdfBookFormatParser.parse(any())).thenReturn("PDF Book Content");
        // Set up parser interactions for docBookFormatParser
        when(docBookFormatParser.parse(any())).thenReturn("DOC Book Content");

        when(booksRepository.findByBookStatus(BookStatus.ACTIVE)).thenReturn(allBooks);

        // Set up parser interactions for pdfBookFormatParser
        when(pdfBookFormatParser.parse(any())).thenReturn("PDF Book Content");
        // Set up parser interactions for docBookFormatParser
        when(docBookFormatParser.parse(any())).thenReturn("DOC Book Content");
        when(booksRepository.count()).thenReturn(2L);
        quoteService.populateCache(cacheSize);

        verify(pdfBookFormatParser).parse(eq(pdfBook));
        verify(docBookFormatParser).parse(eq(docBook));
    }

    @Test
    public void testSaveQuotesFromCache() {
        // Create some sample quotes to be saved
        Quote quote1 = new Quote(1L, "Quote 1", QuoteStatus.FREE, LocalDateTime.now(), null, LocalDateTime.now(), new Book(), null);
        Quote quote2 = new Quote(2L, "Quote 2", QuoteStatus.FREE, LocalDateTime.now(), null, LocalDateTime.now(), new Book(), null);

        // Create a queue with sample quotes
        Queue<Quote> quoteCache = new LinkedList<>();
        quoteCache.offer(quote1);
        quoteCache.offer(quote2);

        // Call the method to save quotes
        quoteService.saveQuotesFromCache(quoteCache);

        // Verify that the quotes were saved using saveAll()
        List<Quote> expectedQuotesToSave = Arrays.asList(quote1, quote2);
        verify(quotesRepository, times(1)).saveAll(expectedQuotesToSave);
    }

    @Test
    public void testSaveQuotesFromCacheWithEmptyCache() {
        // Create an empty queue
        Queue<Quote> quoteCache = new LinkedList<>();

        // Call the method to save quotes
        quoteService.saveQuotesFromCache(quoteCache);

        // Verify that saveAll() is not called since the cache is empty
        verify(quotesRepository, never()).saveAll(any());
    }
}