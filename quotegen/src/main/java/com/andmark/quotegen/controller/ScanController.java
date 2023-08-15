package com.andmark.quotegen.controller;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.dto.BookDTO;
import com.andmark.quotegen.service.BookService;
import com.andmark.quotegen.service.ScanService;
import com.andmark.quotegen.util.impl.Fb2BookFormatParser;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class ScanController {
    private final ScanService scanService;
    private final BookService bookService;
    private final ModelMapper mapper;

    @Autowired
    public ScanController(ScanService scanService, BookService bookService, ModelMapper mapper) {
        this.scanService = scanService;
        this.bookService = bookService;
        this.mapper = mapper;
    }

    @GetMapping("/scan-books")
    public List<BookDTO> scanBooks(@RequestParam String directoryPath) {
        log.debug("log.debug start scan-books");
        List<BookDTO> scannedBooks = scanService.scanBooks(directoryPath);
        log.debug("scannedBooks = " + scannedBooks);
        log.info("finished scannedBooks from scanService.scanBooks({})", directoryPath);
        return scannedBooks;
    }

    @GetMapping("/test-parse")
    public String parseBook(@RequestParam Long id) {
        Fb2BookFormatParser parser = new Fb2BookFormatParser();
        BookDTO bookDTO = new BookDTO();
        bookDTO = bookService.findOne(id);
        return parser.parse(mapper.map(bookDTO, Book.class));
    }

    @GetMapping("/test-connection")
    public ResponseEntity<String> testConnection() {
        log.debug("log.debug test-connection");
        log.info("log.info test-connection");
        return ResponseEntity.ok("Connection is ok");
    }

}
