package com.andmark.qoutegen.service.impl;

import com.andmark.qoutegen.domain.Book;
import com.andmark.qoutegen.domain.enums.Status;
import com.andmark.qoutegen.repository.BooksRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BooksServiceImplTest {

    @InjectMocks
    private BooksServiceImpl booksService;

    @Mock
    private BooksRepository booksRepository;

    @Test
    public void testClearDeletedBooks() {
        // Create a list of deleted books
        List<Book> deletedBooks = new ArrayList<>();
        Book deletedBook1 = new Book();
        deletedBook1.setId(1L);
        deletedBook1.setTitle("Deleted Book 1");
        deletedBook1.setStatus(Status.DELETED);
        deletedBooks.add(deletedBook1);

        Book deletedBook2 = new Book();
        deletedBook2.setId(2L);
        deletedBook2.setTitle("Deleted Book 2");
        deletedBook2.setStatus(Status.DELETED);
        deletedBooks.add(deletedBook2);

        when(booksRepository.findByStatus(Status.DELETED)).thenReturn(deletedBooks);

        // Invoke the clearDeletedBooks method
        booksService.clearDeletedBooks();

        // Verify that deleteAll is called with the list of deleted books
        verify(booksRepository, times(1)).deleteAll(deletedBooks);
    }
}