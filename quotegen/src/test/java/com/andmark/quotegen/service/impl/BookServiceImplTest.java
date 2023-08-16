package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.domain.enums.BookStatus;
import com.andmark.quotegen.repository.BooksRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {

    @InjectMocks
    private BookServiceImpl booksService;

    @Mock
    private BooksRepository booksRepository;

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
}