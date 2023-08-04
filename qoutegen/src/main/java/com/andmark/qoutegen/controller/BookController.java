package com.andmark.qoutegen.controller;

import com.andmark.qoutegen.dto.BookDTO;
import com.andmark.qoutegen.service.ScanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BookController {

    private final ScanService scanService;

    @Autowired
    public BookController(ScanService scanService) {
        this.scanService = scanService;
    }

    @GetMapping("/scan-books")
    public List<BookDTO> scanBooks(@RequestParam String directoryPath) {
        List<BookDTO> scannedBooks = scanService.scanBooks(directoryPath);
        return scannedBooks;
    }

}
