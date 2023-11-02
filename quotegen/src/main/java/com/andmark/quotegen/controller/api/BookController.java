package com.andmark.quotegen.controller.api;

import com.andmark.quotegen.dto.ExtractedLinesDTO;
import com.andmark.quotegen.dto.PageLineRequestDTO;
import com.andmark.quotegen.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/books")
@Slf4j
public class BookController {
    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @DeleteMapping("/clear-deleted")
    public ResponseEntity<String> clearDeletedBooks() {
        log.debug("in clearDeletedBooks");
        bookService.clearDeletedBooks();
        log.info("clearDeletedBooks perform");
        return ResponseEntity.ok("Books with status DELETED cleared");
    }

    @PostMapping("/process-page-and-line")
    public ResponseEntity<ExtractedLinesDTO> processPageAndLine(@RequestBody PageLineRequestDTO requestDTO) {
        log.debug("book controller: get requestDTO");
        // Delegate the processing to your service method
        ExtractedLinesDTO extractedLinesDTO = bookService.processPageAndLineNumber(requestDTO);
        return ResponseEntity.ok(extractedLinesDTO);
    }
}
