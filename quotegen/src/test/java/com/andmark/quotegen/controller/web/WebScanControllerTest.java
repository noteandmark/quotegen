package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.domain.enums.BookFormat;
import com.andmark.quotegen.domain.enums.BookStatus;
import com.andmark.quotegen.dto.BookDTO;
import com.andmark.quotegen.service.ScanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = {"ADMIN"})
@TestPropertySource(properties = "spring.expression.compiler.allowed-classes=java.util.Arrays,java.util.ArrayList")
public class WebScanControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ScanService scanService;

    @Test
    void shouldShowScanBooksPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/admin/scanbooks"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/scanbooks"));
    }

    @Test
    void shouldScanBooksAndReturnScannedBooks() throws Exception {
        // Arrange
        String directoryPath = "/path/to/directory";
//        List<BookDTO> scannedBooks = Arrays.asList(new BookDTO(), new BookDTO());
        BookDTO book1 = BookDTO.builder()
                .id(1L)
                .title("Book 1")
                .author("Author 1")
                .format(BookFormat.FB2)
                .filePath("/path/to/directory/book1")
                .bookStatus(BookStatus.ACTIVE)
                .build();
        BookDTO book2 = BookDTO.builder()
                .id(2L)
                .title("Book 2")
                .author("Author 2")
                .format(BookFormat.PDF)
                .filePath("/path/to/directory/book2")
                .bookStatus(BookStatus.ACTIVE)
                .build();
        List<BookDTO> scannedBooks = Arrays.asList(book1, book2);

        System.out.println("test scannedBooks = " + scannedBooks);
        System.out.println("test scannedBooks size = " + scannedBooks.size());

        when(scanService.scanBooks(directoryPath)).thenReturn(scannedBooks);

        // Act & Assert
        mockMvc.perform(post("/admin/scanbooks")
                        .param("directoryPath", directoryPath))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/scanbooks :: #result-container"))
                .andExpect(model().attribute("scannedBooks", scannedBooks));
    }

}