package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.domain.enums.BookFormat;
import com.andmark.quotegen.domain.enums.BookStatus;
import com.andmark.quotegen.dto.BookDTO;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {

    @InjectMocks
    private BookServiceImpl booksService;
    @Mock
    private BooksRepository booksRepository;
    @Mock
    private QuoteService quoteService;
    @Mock
    private MapperConvert<Book, BookDTO> mapper;
    private BookDTO bookDTO;
    private Book book;

    @BeforeEach
    public void setUp() {
        int countLinesAtPage = 43;
        int maxCharactersInline = 65;
        int numberNextLines = 7;
        List<Book> all = new ArrayList<>();
        BookDTO bookDTO = new BookDTO();
        Book book = new Book();
        lenient().when(mapper.convertToDTO(book,BookDTO.class)).thenReturn(bookDTO);
        lenient().when(mapper.convertToEntity(bookDTO,Book.class)).thenReturn(book);
        bookDTO.setId(1L);
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
        // Mocking
        BookDTO bookDTO = new BookDTO();
        Book book = new Book();

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
        Long bookId = 1L;
        Book book = new Book();
        book.setId(bookId);
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
//        BookDTO bookDTO = new BookDTO();
//        Book book = new Book();
//        when(mapper.convertToDTO(book,BookDTO.class)).thenReturn(bookDTO);
////        when(mapper.convertToEntity(bookDTO,Book.class)).thenReturn(book);
//        bookDTO.setId(1L);
//        bookDTO.setTitle("some title");
//        bookDTO.setAuthor("author");
//        bookDTO.setFormat(BookFormat.FB2);
//        bookDTO.setFilePath("file-path");
        List<Book> all = new ArrayList<>();
        all.add(book);

        when(booksRepository.findAll()).thenReturn(all);
        booksService.findAll();
        verify(booksRepository,only()).findAll();

//        public List<BookDTO> findAll() {
//            log.debug("find all books");
//            List<Book> bookList = booksRepository.findAll();
//            log.info("founded bookList = {}", bookList);
//            return convertToDtoList(bookList);

//        public List<PersonDTO> findAll() {
//            log.debug("getting findAll in personService");
//            List<Person> all = personDAO.findAll();
//            if (all.isEmpty()) {
//                log.warn("there are no any person");
//                throw new ServiceException("Person is empty");
//            }
//            return mapListOfEntityToDTO(all);
//
//
//        List<Person> all = new ArrayList<>();
//        all.add(person);
//        when(personDAO.findAll()).thenReturn(all);
//        personService.findAll();
//        verify(personDAO, only()).findAll();
    }


//    @Test
//    public void testFindAllBooks() {
//        // Mocking
//        Long bookId = 1L;
//        Book book = new Book();
//        book.setId(bookId);
//        BookDTO mockBookDTO = new BookDTO();
//        mockBookDTO.setId(bookId);
//        // Prepare mock data
//
//        List<Book> mockBooks = new ArrayList<>();
//        mockBooks.add(new Book());
//        mockBooks.add(new Book());
//
//        // Mock repository behavior
//        when(booksRepository.findAll()).thenReturn(mockBooks);
//
//        // Mock mapping behavior
//        List<BookDTO> mockBookDTOs = new ArrayList<>();
//        mockBookDTOs.add(new BookDTO());
//        mockBookDTOs.add(new BookDTO());
//        given(mapper.convertToDTO(book)).willReturn(new BookDTO());
////        when(mapper.convertToDTO(book, BookDTO.class)).thenReturn(new BookDTO());
////        when(booksService.convertToDtoList(mockBooks)).thenReturn(mockBookDTOs);
//
//        // Test the service method
//        List<BookDTO> result = booksService.findAll();
//
//        // Verify the result
//        assertEquals(mockBookDTOs.size(), result.size());
//
//    }

}