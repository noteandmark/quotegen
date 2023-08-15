package com.andmark.quotegen.controller;

import com.andmark.quotegen.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
