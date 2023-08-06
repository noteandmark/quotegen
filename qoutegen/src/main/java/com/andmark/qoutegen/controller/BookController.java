package com.andmark.qoutegen.controller;

import com.andmark.qoutegen.service.BooksService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
@Slf4j
public class BookController {
    private final BooksService booksService;

    @Autowired
    public BookController(BooksService booksService) {
        this.booksService = booksService;
    }

    @DeleteMapping("/clear-deleted")
    public ResponseEntity<String> clearDeletedBooks() {
        log.debug("in clearDeletedBooks");
        booksService.clearDeletedBooks();
        log.info("clearDeletedBooks perform");
        return ResponseEntity.ok("Books with status DELETED cleared");
    }
}
