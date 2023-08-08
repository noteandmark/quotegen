package com.andmark.qoutegen.controller;

import com.andmark.qoutegen.domain.Book;
import com.andmark.qoutegen.dto.BookDTO;
import com.andmark.qoutegen.service.BooksService;
import com.andmark.qoutegen.service.ScanService;
import com.andmark.qoutegen.util.impl.Fb2BookFormatParser;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final BooksService booksService;
    private final ModelMapper mapper;

    @Autowired
    public ScanController(ScanService scanService, BooksService booksService, ModelMapper mapper) {
        this.scanService = scanService;
        this.booksService = booksService;
        this.mapper = mapper;
    }

    @GetMapping("/scan-books")
    public List<BookDTO> scanBooks(@RequestParam String directoryPath) {
        log.debug("start scan-books");
        List<BookDTO> scannedBooks = scanService.scanBooks(directoryPath);
        log.debug("scannedBooks = " + scannedBooks);
        log.info("get scannedBooks from scanService.scanBooks(directoryPath)");
        return scannedBooks;
    }

    @GetMapping("/test-parse")
    public String parseBook(@RequestParam Long id) {
        Fb2BookFormatParser parser = new Fb2BookFormatParser();
        BookDTO bookDTO = new BookDTO();
        bookDTO = booksService.findOne(id);
        return parser.parse(mapper.map(bookDTO, Book.class));
    }

}
