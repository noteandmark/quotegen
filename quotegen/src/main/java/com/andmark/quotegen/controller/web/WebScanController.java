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
    public String showScanBooksPage(Model model) {
        try {
            log.debug("WebScanController get showScanBooksPage");
            model.addAttribute("loading", false);
            return "admin/scanbooks";
        } catch (Exception e) {
            log.error("An error occurred while processing the GET request for /admin/scanbooks", e);
            model.addAttribute("error", "An error occurred while processing the request");
            return "public/error";
        }
    }

    @PostMapping("/scanbooks")
    public String scanBooks(@RequestParam(name = "directoryPath") String directoryPath,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        log.debug("WebScanController POST scanBooks");
        log.debug("directoryPath = {}", directoryPath);

        // Set loading to true before scanning
        model.addAttribute("loading", true);

        // Use CompletableFuture to perform scanning asynchronously
        CompletableFuture.supplyAsync(() -> {
            List<BookDTO> scannedBooks = scanService.scanBooks(directoryPath);
            log.debug("scannedBooks count = " + scannedBooks.size());

            return scannedBooks;
        }).thenAccept(scannedBooks -> {
            // After scanning is complete, set loading back to false
            model.addAttribute("loading", false);

            // Add the scanned books to the model
            model.addAttribute("scannedBooks", scannedBooks);
        });

        return "admin/scanbooks";
    }

}
//        List<BookDTO> scannedBooks = scanService.scanBooks(directoryPath);
//        log.debug("scannedBooks count = " + scannedBooks.size());
//
//        redirectAttributes.addFlashAttribute("scannedBooks", scannedBooks);
//
//        // After scanning is complete, set loading back to false
//        model.addAttribute("loading", false);
//
//        return "redirect:/admin/scanbooks";
//    }

