package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.domain.enums.BookFormat;
import com.andmark.quotegen.domain.enums.BookStatus;
import com.andmark.quotegen.dto.BookDTO;
import com.andmark.quotegen.dto.ExtractedLinesDTO;
import com.andmark.quotegen.dto.PageLineRequestDTO;
import com.andmark.quotegen.exception.NotFoundBookException;
import com.andmark.quotegen.repository.BooksRepository;
import com.andmark.quotegen.service.QuoteService;
import com.andmark.quotegen.util.impl.MapperConvert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {
    @Mock
    private BooksRepository booksRepository;
    @Mock
    private QuoteServiceImpl quoteService;
    @Mock
    private MapperConvert<Book, BookDTO> mapper;
    @InjectMocks
    private BookServiceImpl booksService;

    private BookDTO bookDTO;
    private Book book;
    private Long bookId;

    @Value("${quote.countLinesAtPage}")
    private int countLinesAtPage;
    @Value("${quote.maxCharactersInline}")
    private int maxCharactersInline;
    @Value("${quote.numberNextLines}")
    private int numberNextLines;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(booksService, "countLinesAtPage", 43);
        ReflectionTestUtils.setField(booksService, "maxCharactersInline", 65);
        ReflectionTestUtils.setField(booksService, "numberNextLines", 7);

        bookId = 1L;
        bookDTO = new BookDTO();
        book = new Book();
        book.setId(bookId);
        lenient().when(mapper.convertToDTO(book, BookDTO.class)).thenReturn(bookDTO);
        lenient().when(mapper.convertToEntity(bookDTO, Book.class)).thenReturn(book);
        bookDTO.setId(bookId);
        bookDTO.setTitle("some title");
        bookDTO.setAuthor("author");
        bookDTO.setFormat(BookFormat.FB2);
        bookDTO.setFilePath("file-path");
    }

    @Test
    public void testClearDeletedBooks() {
        // Create a list of deleted books
        List<Book> deletedBooks = new ArrayList<>();
        Book deletedBook1 = new Book();
        deletedBook1.setId(1L);
        deletedBook1.setTitle("Deleted Book 1");
        deletedBook1.setBookStatus(BookStatus.DELETED);
        deletedBooks.add(deletedBook1);

        Book deletedBook2 = new Book();
        deletedBook2.setId(2L);
        deletedBook2.setTitle("Deleted Book 2");
        deletedBook2.setBookStatus(BookStatus.DELETED);
        deletedBooks.add(deletedBook2);

        when(booksRepository.findByBookStatus(BookStatus.DELETED)).thenReturn(deletedBooks);

        // Invoke the clearDeletedBooks method
        booksService.clearDeletedBooks();
        // Verify that deleteAll is called with the list of deleted books
        verify(booksRepository, times(1)).deleteAll(deletedBooks);
    }

    @Test
    public void testSaveBook() {
        // Mocking behavior
        when(booksService.convertToEntity(bookDTO)).thenReturn(book);
        // Test
        booksService.save(bookDTO);
        // Verification
        verify(booksRepository).save(book);
    }

    @Test
    public void testFindOneBook() {
        // Mocking
        BookDTO mockBookDTO = new BookDTO();
        mockBookDTO.setId(bookId);
        // Mocking behavior
        when(booksRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(booksService.convertToDTO(book)).thenReturn(mockBookDTO);
        // Test
        BookDTO result = booksService.findOne(bookId);
        // Verification
        assertEquals(mockBookDTO, result);
    }

    @Test
    public void testFindAllBooks() {
        List<Book> all = new ArrayList<>();
        all.add(book);

        when(booksRepository.findAll()).thenReturn(all);
        booksService.findAll();
        verify(booksRepository, only()).findAll();
    }

    @Test
    public void testUpdateBook() {
        // Mocking
        BookDTO updatedBookDTO = new BookDTO();
        updatedBookDTO.setId(bookId);
        updatedBookDTO.setTitle("new title");
        Book updatedBook = new Book();
        updatedBook.setId(bookId);
        updatedBook.setTitle(updatedBookDTO.getTitle());

        // Mocking behavior
        when(booksService.convertToEntity(updatedBookDTO)).thenReturn(updatedBook);
        // Test
        booksService.update(bookId, updatedBookDTO);
        // Verification
        verify(booksRepository).save(updatedBook);
    }

    @Test
    public void testDeleteBook() {
        // Test
        booksService.delete(bookId);
        // Verification
        verify(booksRepository).deleteById(bookId);
    }

    @Test
    public void testProcessPageAndLineNumber() {
        // Mock PageLineRequestDTO
        PageLineRequestDTO requestDTO = new PageLineRequestDTO();
        requestDTO.setPageNumber(1); // Set the desired page number
        requestDTO.setLineNumber(1); // Set the desired line number
        // Mock selected book
        Book mockSelectedBook = new Book();

        // Mock active books
        List<Book> mockActiveBooks = new ArrayList<>();
        mockActiveBooks.add(new Book());
        when(booksRepository.findByBookStatus(BookStatus.ACTIVE)).thenReturn(mockActiveBooks);

        // Mock book content
        String mockBookContent = "This is a mock book content.";
        when(quoteService.getBookText(mockSelectedBook)).thenReturn(mockBookContent);

        // Call the method being tested
        ExtractedLinesDTO result = booksService.processPageAndLineNumber(requestDTO);

        // Verify the repository method and service method were called
        verify(booksRepository).findByBookStatus(BookStatus.ACTIVE);
        verify(quoteService).getBookText(mockSelectedBook);
    }


    @Test
    public void testGetActiveBooks() {
        // Prepare mock data
        when(booksRepository.findByBookStatus(BookStatus.ACTIVE)).thenReturn(List.of(book));

        // Call the method being tested
        List<Book> result = booksService.getActiveBooks();

        // Verify the repository method was called
        verify(booksRepository).findByBookStatus(BookStatus.ACTIVE);

        // Perform assertions
        assertEquals(List.of(book), result);
    }

    @Test
    public void testGetActiveBooksWithNoActiveBooks() {
        // Prepare mock data
        when(booksRepository.findByBookStatus(BookStatus.ACTIVE)).thenReturn(Collections.emptyList());
        // Call the method being tested
        assertThrows(NotFoundBookException.class, () -> booksService.getActiveBooks());
        // Verify the repository method was called
        verify(booksRepository).findByBookStatus(BookStatus.ACTIVE);
    }

    @Test
    public void testSelectRandomBook() {
        // Prepare mock data
        List<Book> mockActiveBooks = new ArrayList<>();
        Book book1 = new Book();
        book1.setId(1L);
        mockActiveBooks.add(book1);
        Book book2 = new Book();
        book2.setId(2L);
        mockActiveBooks.add(book2);

        // Call the method being tested
        Book result = booksService.selectRandomBook(mockActiveBooks);

        // Perform assertions
        assertTrue(mockActiveBooks.contains(result));
    }

    @Test
    public void testGetBookContent() {
        // Prepare mock data
        Book selectedBook = new Book();
        String expectedBookContent = "This is the book content.";
        when(quoteService.getBookText(selectedBook)).thenReturn(expectedBookContent);

        // Call the method being tested
        String result = booksService.getBookContent(selectedBook);

        // Verify the mock method was called
        verify(quoteService).getBookText(selectedBook);

        // Perform assertions
        assertEquals(expectedBookContent, result);
    }

    @Test
    public void testBreakContentIntoLines() {
        String bookContent = "This is a sample text that needs to be broken into lines without breaking words.";

        List<String> result = booksService.breakContentIntoLines(bookContent);

        // Verify the result
        List<String> expectedLines = Arrays.asList(
                "This is a sample text that needs to be broken into lines without",
                "breaking words."
        );

        assertEquals(expectedLines.size(), result.size());
        for (int i = 0; i < expectedLines.size(); i++) {
            assertEquals(expectedLines.get(i), result.get(i));
        }
    }

    @Test
    public void testExtractRequestedLines() {
        List<String> lines = Arrays.asList(
                "Line 1", "Line 2", "Line 3", "Line 4", "Line 5",
                "Line 6", "Line 7", "Line 8", "Line 9", "Line 10"
        );

        int userLineIndex = 2; // Starting from "Line 3"

        List<String> expectedExtractedLines = Arrays.asList("Line 3", "Line 4", "Line 5", "Line 6", "Line 7", "Line 8", "Line 9");

        List<String> result = booksService.extractRequestedLines(lines, userLineIndex);

        assertEquals(expectedExtractedLines, result);
    }


}