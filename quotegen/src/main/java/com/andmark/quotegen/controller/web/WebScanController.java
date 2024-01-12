package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.BookDTO;
import com.andmark.quotegen.service.ScanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    public String scanBooks(@RequestParam(name = "directoryPath") String directoryPath,
                            Model model) {
        log.debug("WebScanController POST scanBooks");
        log.debug("directoryPath = {}", directoryPath);

        List<BookDTO> scannedBooks = scanService.scanBooks(directoryPath);
        log.debug("scannedBooks count = " + scannedBooks.size());

        model.addAttribute("scannedBooks", scannedBooks);

        return "admin/scanbooks :: #result-container";
    }

}

