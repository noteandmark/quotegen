package com.andmark.qoutegen.service;

import com.andmark.qoutegen.dto.BookDTO;
import com.andmark.qoutegen.models.Book;
import com.andmark.qoutegen.models.enums.BookFormat;
import com.andmark.qoutegen.models.enums.Status;
import com.andmark.qoutegen.repository.BooksRepository;
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

    @Test
    public void whenGetRootDirectory_thenShouldCheckThatIsRealDirectory() throws IOException {
        tempDirectory = createTempDirectory();
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
        tempDirectory = createTempDirectory();
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

        Book existingBook = new Book();
        existingBook.setId(null);
        existingBook.setTitle("existingFile");
        existingBook.setAuthor("file.getParentFile().getName()");
        existingBook.setFilePath("existingFile.epub");
        existingBook.setStatus(Status.ACTIVE);
        existingBook.setFormat(BookFormat.EPUB);

        Book resultBook = scanService.processBookFile(existingFile);
        assertThat(resultBook, notNullValue());
        verify(booksRepository).findByFilePath(any());
        verify(scanService, times(1)).checkExistingBook(existingFile);
        verify(scanService, times(1)).getBookFormat(existingFile.getName());
        assertEquals(existingBook.getFilePath(), resultBook.getFilePath());
        // Verify that the status of existingBook is not changed
        assertEquals(Status.ACTIVE, existingBook.getStatus());
    }

    @Test
    public void whenNewBookFound_thenShouldReturnBook() {
        File newFile = new File("newFile.pdf");

        when(scanService.checkExistingBook(newFile)).thenReturn(null); // No existing book
        Book newResult = scanService.processBookFile(newFile);
        assertNotNull(newResult);
        // Verify that the getBookFormat method is called for newFile
        verify(scanService, times(1)).getBookFormat(newFile.getName());
        verify(scanService, times(1)).checkExistingBook(newFile);

        // Verify that a new book is created for newFile
        assertEquals("newFile", newResult.getTitle());
        assertEquals(BookFormat.PDF, newResult.getFormat());
        assertEquals(Status.ACTIVE, newResult.getStatus());
    }

    @Test
    public void whenUnknownFileFound_thenShouldSkipBook() {
        File unknownFile = new File("unknownFile.xyz");

        when(scanService.getBookFormat(unknownFile.getName())).thenReturn(BookFormat.NOT_FOUND);
        Book unknownResult = scanService.processBookFile(unknownFile);

        // Verify that the getBookFormat method is called for existingFile, newFile, and unknownFile
        verify(scanService, times(1)).getBookFormat(unknownFile.getName());
        // Verify that null is returned for unknownFile
        assertNull(unknownResult);
    }

    @Test
    public void whenValidBookFormat_thenShouldReturnCorrectEnum() {
        assertEquals(BookFormat.EPUB, scanService.getBookFormat("book.epub"));
        assertEquals(BookFormat.FB2, scanService.getBookFormat("book.fb2"));
        assertEquals(BookFormat.PDF, scanService.getBookFormat("book.pdf"));
        assertEquals(BookFormat.DOC, scanService.getBookFormat("book.doc"));
        assertEquals(BookFormat.DOCX, scanService.getBookFormat("book.docx"));
        assertEquals(BookFormat.NOT_FOUND, scanService.getBookFormat("book.unknown"));
    }

    @Test
    public void whenInvalidBookFormat_thenShouldReturnNotFound() {
        assertEquals(BookFormat.NOT_FOUND, scanService.getBookFormat("book"));
        assertEquals(BookFormat.NOT_FOUND, scanService.getBookFormat("book."));
        assertEquals(BookFormat.NOT_FOUND, scanService.getBookFormat("book.epu"));
    }

    @Test
    public void whenFileNameWithExtension_thenShouldRemoveExtension() {
        assertEquals("file", scanService.removeExtension(new File("file.txt")));
        assertEquals("document", scanService.removeExtension(new File("document.docx")));
        assertEquals("data", scanService.removeExtension(new File("data.csv")));
        assertEquals("image", scanService.removeExtension(new File("image.jpg")));
    }

    @Test
    public void whenFileNameWithoutExtension_thenShouldReturnOriginalName() {
        assertEquals("file", scanService.removeExtension(new File("file")));
        assertEquals("document", scanService.removeExtension(new File("document")));
        assertEquals("data", scanService.removeExtension(new File("data")));
        assertEquals("image", scanService.removeExtension(new File("image")));
    }

    private File createTempDirectory() throws IOException {
        return Files.createTempDirectory("testDir").toFile();
    }

}