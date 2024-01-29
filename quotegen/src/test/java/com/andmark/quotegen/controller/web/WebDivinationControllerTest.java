package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.ExtractedLinesDTO;
import com.andmark.quotegen.dto.PageLineRequestDTO;
import com.andmark.quotegen.service.BookService;
import com.andmark.quotegen.service.WebDivinationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = {"USER"})
public class WebDivinationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookService bookService;
    @MockBean
    private WebDivinationService webDivinationService;

    @Test
    void shouldPerformDivination() throws Exception {
        // Arrange
        int pageNumber = 1;
        int lineNumber = 1;
        MockHttpSession session = new MockHttpSession();
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(webDivinationService.checkPerformedDivination(session)).thenReturn(false);

        PageLineRequestDTO requestDTO = new PageLineRequestDTO(pageNumber, lineNumber);
        ExtractedLinesDTO extractedLinesDTO = new ExtractedLinesDTO();
        extractedLinesDTO.setLines(Arrays.asList("line1", "line2"));
        extractedLinesDTO.setBookAuthor("Author");
        extractedLinesDTO.setBookTitle("Title");
        when(bookService.processPageAndLineNumber(requestDTO)).thenReturn(extractedLinesDTO);

        // Act & Assert
        mockMvc.perform(post("/web/divination")
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("lineNumber", String.valueOf(lineNumber))
                        .session(session)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("web/divination-result"))
                .andExpect(model().attribute("extractedLines", extractedLinesDTO.getLines()))
                .andExpect(model().attribute("bookAuthor", extractedLinesDTO.getBookAuthor()))
                .andExpect(model().attribute("bookTitle", extractedLinesDTO.getBookTitle()));

        verify(webDivinationService).markUserAsPerformedDivinationSession(session);
    }
}