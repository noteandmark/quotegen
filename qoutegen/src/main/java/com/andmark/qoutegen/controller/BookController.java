package com.andmark.qoutegen.controller;

import com.andmark.qoutegen.dto.BookDTO;
import com.andmark.qoutegen.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/scan-books")
    public List<BookDTO> scanBooks(@RequestParam String directoryPath) {
        List<BookDTO> scannedBooks = bookService.scanBooks(directoryPath);
        System.out.println("finished");
        return scannedBooks;
    }

}
