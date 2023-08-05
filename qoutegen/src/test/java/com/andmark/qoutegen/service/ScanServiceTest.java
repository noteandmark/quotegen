package com.andmark.qoutegen.service;

import com.andmark.qoutegen.dto.BookDTO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        List<BookDTO> expectedBooks = new ArrayList<>();
        // Mock behavior of getBookDTOList
        when(scanService.getBookDTOList(tempDirectory)).thenReturn(expectedBooks);

        List<BookDTO> result = scanService.scanBooks(tempDirectory.getAbsolutePath());

        assertEquals(expectedBooks.size(), result.size());
        verify(scanService, times(2)).getBookDTOList(tempDirectory);
    }

    @Test
    public void testScanBooks_InvalidDirectoryPath() {
        assertThrows(IllegalArgumentException.class, () -> {
            scanService.scanBooks("nonExistentDirectoryPath");
        });
    }

    private File createTempDirectory() throws IOException {
        return Files.createTempDirectory("testDir").toFile();
    }
}