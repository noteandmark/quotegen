package com.andmark.qoutegen.service;

import com.andmark.qoutegen.dto.BookDTO;
import com.andmark.qoutegen.model.Book;
import com.andmark.qoutegen.repository.BooksRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScanServiceTest {

    @InjectMocks
    private ScanService scanService;

    @Mock
    private BooksRepository booksRepository;
    @Mock
    private ModelMapper mapper;

//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.initMocks(this);
//        mapper = new ModelMapper();
//        scanService = new ScanService(booksRepository, mapper);
//    }

    @Test
    public void testScanBooks() {
        File rootDirectory = mock(File.class);
        when(rootDirectory.exists()).thenReturn(true);
        when(rootDirectory.isDirectory()).thenReturn(true);

        // Mock the recursive scan
        List<BookDTO> scannedBooks = new ArrayList<>();
        scanService.scanBooksRecursive(rootDirectory, scannedBooks);

        // Call the scanBooks method with a mock directory path
        List<BookDTO> result = scanService.scanBooks("mockDirectoryPath");

        // Assertions
        assertEquals(scannedBooks.size(), result.size());
        verify(booksRepository, times(scannedBooks.size())).save(any(Book.class));


//        List<BookDTO> scannedBooks = new ArrayList<>();
//        when(booksRepository.save(any(Book.class))).thenReturn(new Book());
//
//        scanService.scanBooksRecursive(rootDirectory, scannedBooks);
//
//        List<BookDTO> result = scanService.scanBooks("directoryPath");
//
//        assertEquals(scannedBooks.size(), result.size());
//        verify(booksRepository, times(scannedBooks.size())).save(any(Book.class));
    }

}