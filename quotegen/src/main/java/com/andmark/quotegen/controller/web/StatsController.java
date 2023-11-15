package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.StatsDTO;
import com.andmark.quotegen.service.ScanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatsController {
    private final ScanService scanService;

    @Autowired
    public StatsController(ScanService scanService) {
        this.scanService = scanService;
    }

    @GetMapping("/stats")
    public String showStats(Model model) {
        StatsDTO stats = scanService.getStatistics();

        model.addAttribute("bookCount", stats.getBookCount());
        model.addAttribute("publishedQuotesThisYear", stats.getPublishedQuotesThisYear());
        model.addAttribute("pendingQuotesCount", stats.getPendingQuotesCount());

        return "public/stats";
    }
}
