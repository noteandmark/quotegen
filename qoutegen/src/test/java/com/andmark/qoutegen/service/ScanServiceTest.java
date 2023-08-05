package com.andmark.qoutegen.service;

import com.andmark.qoutegen.dto.BookDTO;
import com.andmark.qoutegen.model.Book;
import com.andmark.qoutegen.model.enums.BookFormat;
import com.andmark.qoutegen.model.enums.Status;
import com.andmark.qoutegen.repository.BooksRepository;
import com.andmark.qoutegen.service.ScanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScanServiceTest {

    @InjectMocks
    @Spy
    private ScanService scanService;

    @Mock
    private BooksRepository booksRepository;

    @Mock
    private ModelMapper mapper;

    private File tempDirectory;

    @BeforeEach
    public void setUp() throws IOException {
        tempDirectory = createTempDirectory();
    }

    @Test
    public void whenGetRootDirectory_thenShouldCheckThatIsRealDirectory() {
        List<Book> expectedBooks = new ArrayList<>();
        // Mock behavior of getBookDTOList
        when(scanService.getBookList(tempDirectory)).thenReturn(expectedBooks);

        List<BookDTO> result = scanService.scanBooks(tempDirectory.getAbsolutePath());

        assertEquals(expectedBooks.size(), result.size());
        verify(scanService, times(1)).getBookList(tempDirectory);
        verify(booksRepository, times(1)).saveAll(any());
    }

    @Test
    public void testScanBooks_InvalidDirectoryPath() {
        assertThrows(IllegalArgumentException.class, () -> {
            scanService.scanBooks("nonExistentDirectoryPath");
        });
    }

    @Test
    public void testScanBooks_ValidDirectoryPath() throws IOException {
        // Create temporary files within the temporary directory
        File file1 = new File(tempDirectory, "book1.epub");
        file1.createNewFile();
        File file2 = new File(tempDirectory, "book2.pdf");
        file2.createNewFile();

        // Create a list of expected scanned books
        List<Book> expectedScannedBooks = new ArrayList<>();
        Book book1 = new Book();
        book1.setAuthor("Author1");
        book1.setTitle("Book 1");
        book1.setFilePath(file1.getAbsolutePath());
        book1.setFormat(BookFormat.EPUB);
        book1.setStatus(Status.ACTIVE);
        expectedScannedBooks.add(book1);

        Book book2 = new Book();
        book2.setAuthor("Author2");
        book2.setTitle("Book 2");
        book2.setFilePath(file2.getAbsolutePath());
        book2.setFormat(BookFormat.PDF);
        book2.setStatus(Status.ACTIVE);
        expectedScannedBooks.add(book2);

        // Mock behavior of getBookList
        when(scanService.getBookList(tempDirectory)).thenReturn(expectedScannedBooks);

        // Perform the test
        List<BookDTO> result = scanService.scanBooks(tempDirectory.getAbsolutePath());

        // Verify
        assertEquals(expectedScannedBooks.size(), result.size());
        verify(scanService, times(2)).getBookList(tempDirectory);
        verify(booksRepository, times(1)).saveAll(expectedScannedBooks);
    }

    @Test
    public void whenCleanUpDatabase_thenShouldDeletedBooks() {
        List<Book> scannedBooks = new ArrayList<>();
        List<Book> booksInDatabase = new ArrayList<>();

        Book bookInDatabase1 = new Book();
        bookInDatabase1.setId(1L);
        bookInDatabase1.setTitle("Existing Book 1");
        bookInDatabase1.setStatus(Status.ACTIVE);
        booksInDatabase.add(bookInDatabase1);

        Book bookInDatabase2 = new Book();
        bookInDatabase2.setId(2L);
        bookInDatabase2.setTitle("Existing Book 2");
        bookInDatabase2.setStatus(Status.ACTIVE);
        booksInDatabase.add(bookInDatabase2);

        when(booksRepository.findByStatus(Status.ACTIVE)).thenReturn(booksInDatabase);

        // Simulate a scenario where bookInDatabase1 has been deleted from disk
        // and bookInDatabase2 still exists in the scanned books
        scannedBooks.add(bookInDatabase2);

        // Act
        scanService.cleanUpDatabase(scannedBooks);

        assertEquals(Status.DELETED, bookInDatabase1.getStatus());

        // Verify that the save method is called exactly once for bookInDatabase1
        verify(booksRepository, times(1)).save(bookInDatabase1);
        // Verify that the save method is never called for bookInDatabase2
        verify(booksRepository, never()).save(bookInDatabase2);
    }

    @Test
    public void whenExistingBookFound_thenShouldReturnBook() {
        File existingFile = new File("existingFile.epub");

        Book expected = new Book();
        expected.setId(null);
        expected.setTitle("existingFile");
        expected.setAuthor("file.getParentFile().getName()");
        expected.setFilePath("existingFile.epub");
        expected.setStatus(Status.ACTIVE);
        expected.setFormat(BookFormat.EPUB);

        Book actual = scanService.processBookFile(existingFile);
        assertThat(actual, notNullValue());
        verify(booksRepository).findByFilePath(any());
        verify(scanService, times(1)).checkExistingBook(existingFile);
        verify(scanService, times(1)).getBookFormat(existingFile.getName());
        assertEquals(expected, actual);

//        File unknownFile = new File("unknownFile.xyz");

        // Mock behavior of checkExistingBook
//        Book existingBook = new Book();
//        existingBook.setId(1L);
//        existingBook.setTitle("Existing Book")
//        existingBook.setStatus(Status.ACTIVE);
//        when(scanService.checkExistingBook(existingFile)).thenReturn(existingBook);

        // Mock behavior of getBookFormat
//        when(scanService.getBookFormat(newFile.getName())).thenReturn(BookFormat.PDF);
//        when(scanService.getBookFormat(unknownFile.getName())).thenReturn(BookFormat.NOT_FOUND);

        // Act
//        List<BookDTO> result = scanService.scanBooks(tempDirectory.getAbsolutePath());

//        when(scanService.getBookFormat(existingFile.getName())).thenReturn(BookFormat.EPUB);
//        when(booksRepository.findByFilePath(existingFile.getPath())).thenReturn(Optional.of(existingBook));
//        Book existingResult = scanService.processBookFile(existingFile);
//        Book unknownResult = scanService.processBookFile(unknownFile);

        // Assert
        // Verify that the checkExistingBook method is called for existingFile and newFile
//        verify(scanService, times(1)).checkExistingBook(newFile);

        // Verify that the getBookFormat method is called for existingFile, newFile, and unknownFile
//        verify(scanService, times(1)).getBookFormat(existingFile.getName());
//        verify(scanService, times(1)).getBookFormat(newFile.getName());
//        verify(scanService, times(1)).getBookFormat(unknownFile.getName());

        // Verify that the status of existingBook is not changed
//        assertEquals(Status.ACTIVE, existingResult.getStatus());

        // Verify that a new book is created for newFile
//        assertNotNull(newResult);
//        assertEquals("newFile", newResult.getTitle());
//        assertEquals(BookFormat.PDF, newResult.getFormat());
//        assertEquals(Status.ACTIVE, newResult.getStatus());
//
//        // Verify that null is returned for unknownFile
//        assertNull(unknownResult);
    }

    @Test
    public void whenNewBookFound_thenShouldReturnBook() {
        File newFile = new File("newFile.pdf");

        when(scanService.checkExistingBook(newFile)).thenReturn(null); // No existing book
        Book newResult = scanService.processBookFile(newFile);

    }


    private File createTempDirectory() throws IOException {
        return Files.createTempDirectory("testDir").toFile();
    }

}