package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.domain.enums.BookFormat;
import com.andmark.quotegen.domain.enums.BookStatus;
import com.andmark.quotegen.dto.BookDTO;
import com.andmark.quotegen.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = {"ADMIN"})
public class WebAdminBookControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookService bookService;

    @Test
    void shouldReturnBookListPage() throws Exception {
        // Arrange
        BookDTO book1 = BookDTO.builder()
                .id(1L)
                .title("Book 1")
                .author("Author 1")
                .format(BookFormat.FB2)
                .filePath("/path/to/book1")
                .bookStatus(BookStatus.ACTIVE)
                .build();
        BookDTO book2 = BookDTO.builder()
                .id(2L)
                .title("Book 2")
                .author("Author 2")
                .format(BookFormat.PDF)
                .filePath("/path/to/book2")
                .bookStatus(BookStatus.ACTIVE)
                .build();
        List<BookDTO> books = Arrays.asList(book1, book2);
        when(bookService.findAll()).thenReturn(books);

        // Act & Assert
        mockMvc.perform(get("/admin/book"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book/list"))
                .andExpect(model().attribute("books", hasSize(2)))
                .andExpect(model().attribute("books", contains(
                        allOf(
                                hasProperty("id", is(1L)),
                                hasProperty("title", is("Book 1")),
                                hasProperty("author", is("Author 1")),
                                hasProperty("format", is(BookFormat.FB2)),
                                hasProperty("filePath", is("/path/to/book1")),
                                hasProperty("bookStatus", is(BookStatus.ACTIVE))
                        ),
                        allOf(
                                hasProperty("id", is(2L)),
                                hasProperty("title", is("Book 2")),
                                hasProperty("author", is("Author 2")),
                                hasProperty("format", is(BookFormat.PDF)),
                                hasProperty("filePath", is("/path/to/book2")),
                                hasProperty("bookStatus", is(BookStatus.ACTIVE))
                        )
                )));
    }

    @Test
    void shouldReturnEmptyBookListPage() throws Exception {
        // Arrange
        when(bookService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/admin/book"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book/list"))
                .andExpect(model().attribute("books", empty()));
    }

    @Test
    void shouldReturnViewBookPage() throws Exception {
        // Arrange
        long bookId = 1L;
        BookDTO bookDTO = BookDTO.builder()
                .id(bookId)
                .title("Book Title")
                .author("Book Author")
                .format(BookFormat.PDF)
                .filePath("/path/to/book")
                .bookStatus(BookStatus.ACTIVE)
                .build();
        when(bookService.findOne(bookId)).thenReturn(bookDTO);

        // Act & Assert
        mockMvc.perform(get("/admin/book/view/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book/view"))
                .andExpect(model().attribute("bookDTO", bookDTO));
    }

    @Test
    void shouldRedirectToAdminBookIfBookNotFound() throws Exception {
        // Arrange
        long nonExistentBookId = 100L;
        when(bookService.findOne(nonExistentBookId)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/admin/book/view/{id}", nonExistentBookId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/book"));
    }

    @Test
    void shouldReturnEditBookFormWithoutQuoteId() throws Exception {
        // Arrange
        long bookId = 1L;
        String quoteId = null;
        BookDTO bookDTO = createBookDTO(bookId);
        when(bookService.findOne(bookId)).thenReturn(bookDTO);

        // Act & Assert
        mockMvc.perform(get("/admin/book/edit/{id}", bookId)
                        .param("quoteId", quoteId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book/edit"))
                .andExpect(model().attribute("bookDTO", bookDTO))
                .andExpect(model().attribute("quoteId", quoteId));
    }

    @Test
    void shouldReturnEditBookFormWithQuoteId() throws Exception {
        // Arrange
        long bookId = 1L;
        String quoteId = "123";
        BookDTO bookDTO = createBookDTO(bookId);
        when(bookService.findOne(bookId)).thenReturn(bookDTO);

        // Act & Assert
        mockMvc.perform(get("/admin/book/edit/{id}", bookId)
                        .param("quoteId", quoteId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book/edit"))
                .andExpect(model().attribute("bookDTO", bookDTO))
                .andExpect(model().attribute("quoteId", quoteId));
    }

    @Test
    void shouldEditBookAndRedirectToQuoteViewPageWithQuoteId() throws Exception {
        // Arrange
        long bookId = 1L;
        String quoteId = "123";
        BookDTO bookDTO = createBookDTO(bookId);
        when(bookService.update(bookDTO)).thenReturn(bookDTO);

        // Act & Assert
        mockMvc.perform(post("/admin/book/edit/{id}", bookId)
                        .param("quoteId", quoteId)
                        .flashAttr("bookDTO", bookDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/quote/view/" + quoteId));
    }

    @Test
    void shouldEditBookAndRedirectToQuoteListPageWithoutQuoteId() throws Exception {
        // Arrange
        long bookId = 1L;
        BookDTO bookDTO = createBookDTO(bookId);
        when(bookService.update(bookDTO)).thenReturn(bookDTO);

        // Act & Assert
        mockMvc.perform(post("/admin/book/edit/{id}", bookId)
                        .flashAttr("bookDTO", bookDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/quote"));
    }

    @Test
    void shouldViewDeletedBooksAndReturnViewDeletedPageWithBooks() throws Exception {
        // Arrange
        Book deletedBook1 = new Book();
        deletedBook1.setId(1L);
        deletedBook1.setTitle("Deleted Book 1");
        deletedBook1.setAuthor("Author 1");
        deletedBook1.setFormat(BookFormat.PDF);
        deletedBook1.setFilePath("/path/to/book1");
        deletedBook1.setBookStatus(BookStatus.DELETED);

        Book deletedBook2 = new Book();
        deletedBook2.setId(2L);
        deletedBook2.setTitle("Deleted Book 2");
        deletedBook2.setAuthor("Author 2");
        deletedBook2.setFormat(BookFormat.FB2);
        deletedBook2.setFilePath("/path/to/book2");
        deletedBook2.setBookStatus(BookStatus.DELETED);

        List<Book> deletedBooks = Arrays.asList(deletedBook1, deletedBook2);

        when(bookService.getDeletedBooks()).thenReturn(deletedBooks);

        // Act & Assert
        mockMvc.perform(get("/admin/book/view-deleted"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book/view-deleted"))
                .andExpect(model().attributeExists("deletedBooks"))
                .andExpect(model().attribute("deletedBooks", hasSize(2)))
                .andExpect(model().attribute("deletedBooks", containsInAnyOrder(
                        allOf(
                                hasProperty("id", is(1L)),
                                hasProperty("title", is("Deleted Book 1")),
                                hasProperty("author", is("Author 1")),
                                hasProperty("format", is(BookFormat.PDF)),
                                hasProperty("filePath", is("/path/to/book1")),
                                hasProperty("bookStatus", is(BookStatus.DELETED))
                        ),
                        allOf(
                                hasProperty("id", is(2L)),
                                hasProperty("title", is("Deleted Book 2")),
                                hasProperty("author", is("Author 2")),
                                hasProperty("format", is(BookFormat.FB2)),
                                hasProperty("filePath", is("/path/to/book2")),
                                hasProperty("bookStatus", is(BookStatus.DELETED))
                        )
                )));
    }

    @Test
    void shouldCleanDataBaseAndRedirectToViewDeletedPage() throws Exception {
        // Arrange
        doNothing().when(bookService).clearDeletedBooks();

        // Act & Assert
        mockMvc.perform(post("/admin/book/clean-db"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/book/view-deleted"));
    }

    private BookDTO createBookDTO(long id) {
        // Create and return a BookDTO instance with the given id (you can customize other fields as needed)
        return BookDTO.builder()
                .id(id)
                .title("Book Title")
                .author("Book Author")
                .format(BookFormat.PDF)
                .filePath("/path/to/book")
                .bookStatus(BookStatus.ACTIVE)
                .build();
    }

}