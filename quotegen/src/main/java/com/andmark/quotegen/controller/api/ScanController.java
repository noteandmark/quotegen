package com.andmark.quotegen.controller.api;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.dto.BookDTO;
import com.andmark.quotegen.dto.StatsDTO;
import com.andmark.quotegen.exception.InvalidWebLinkException;
import com.andmark.quotegen.service.BookService;
import com.andmark.quotegen.service.ScanService;
import com.andmark.quotegen.util.impl.Fb2BookFormatParser;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private final Fb2BookFormatParser parser;

    @Autowired
    public ScanController(ScanService scanService, BookService bookService, ModelMapper mapper, Fb2BookFormatParser parser) {
        this.scanService = scanService;
        this.bookService = bookService;
        this.mapper = mapper;
        this.parser = parser;
    }

    @GetMapping("/scan-books")
    public List<BookDTO> scanBooks(@RequestParam String directoryPath) {
        log.debug("log.debug start scan-books");
        List<BookDTO> scannedBooks = scanService.scanBooks(directoryPath);
        log.debug("scannedBooks count = " + scannedBooks.size());
        log.info("finished scannedBooks from scanService.scanBooks({})", directoryPath);
        return scannedBooks;
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsDTO> getStatistics() {
        log.debug("scan controller: getStatistics");
        StatsDTO stats = scanService.getStatistics();
        log.debug("scan controller: get stats, getting ready to send");
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/test-parse-fb2")
    public String parseBook(@RequestParam Long id) {
        log.debug("scan controller: test parseBook");
        BookDTO bookDTO = bookService.findOne(id);
        Book book = mapper.map(bookDTO, Book.class);
        String parsedText = parser.parse(book);
        return parsedText;
    }

    @GetMapping("/test-connection")
    public ResponseEntity<String> testConnection() {
        log.info("log.info test-connection");
        return ResponseEntity.ok("Connection is ok");
    }

    @GetMapping("/web-link")
    public ResponseEntity<String> getWebLink() {
        log.debug("scan controller: get web link");

        try {
            String webLink = scanService.getWebLink();
            return ResponseEntity.ok(webLink);
        } catch (InvalidWebLinkException e) {
            // Handling an invalid web link error
            log.error("Error getting web link: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid web link: " + e.getMessage());
        } catch (Exception e) {
            // Handling other errors
            log.error("Error getting web link: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while getting web link");
        }
    }

}
