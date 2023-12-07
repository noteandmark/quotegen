package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.BookDTO;
import com.andmark.quotegen.service.ScanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin")
@Slf4j
public class WebScanController {
    private final ScanService scanService;

    @Autowired
    public WebScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @GetMapping("/scanbooks")
    public String showScanBooksPage() {
        log.debug("WebScanController get showScanBooksPage");
        return "admin/scanbooks";
    }

    @PostMapping("/scanbooks")
    public String scanBooks(@RequestParam("directoryPath") String directoryPath, Model model) {
        log.debug("WebScanController post scanBooks");
        List<BookDTO> scannedBooks = scanService.scanBooks(directoryPath);
        log.debug("scannedBooks count = " + scannedBooks.size());
        model.addAttribute("scannedBooks", scannedBooks);
        return "admin/scanbooks";
    }

}
