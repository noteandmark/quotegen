package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.StatsDTO;
import com.andmark.quotegen.service.ScanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/public")
@Slf4j
public class PublicController {
    private final ScanService scanService;

    @Autowired
    public PublicController(ScanService scanService) {
        this.scanService = scanService;
    }

    @GetMapping("/stats")
    public String showStats(Model model) {
        log.debug("public controller showStats");
        StatsDTO stats = scanService.getStatistics();

        model.addAttribute("bookCount", stats.getBookCount());
        model.addAttribute("publishedQuotesThisYear", stats.getPublishedQuotesThisYear());
        model.addAttribute("pendingQuotesCount", stats.getPendingQuotesCount());
        log.debug("showStats added models");

        return "public/stats";
    }

    @GetMapping("/help")
    public String showHelpPage() {
        log.debug("public controller showHelpPage");
        return "public/help";
    }

    @GetMapping("/da-net")
    public String showYesNoMagic() {
        log.debug("public controller showYesNoMagic");
        return "public/da-net";
    }
}
