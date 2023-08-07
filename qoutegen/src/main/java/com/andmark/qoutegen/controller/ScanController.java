package com.andmark.qoutegen.controller;

import com.andmark.qoutegen.dto.BookDTO;
import com.andmark.qoutegen.service.ScanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class ScanController {
    private final ScanService scanService;

    @Autowired
    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @GetMapping("/scan-books")
    public List<BookDTO> scanBooks(@RequestParam String directoryPath) {
        log.debug("start scan-books");
        List<BookDTO> scannedBooks = scanService.scanBooks(directoryPath);
        log.debug("scannedBooks = " + scannedBooks);
        log.info("get scannedBooks from scanService.scanBooks(directoryPath)");
        return scannedBooks;
    }

}
