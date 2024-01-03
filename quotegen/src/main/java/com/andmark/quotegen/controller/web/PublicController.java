package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.StatsDTO;
import com.andmark.quotegen.service.EmailService;
import com.andmark.quotegen.service.ScanService;
import com.andmark.quotegen.service.WebVersionService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/public")
@Slf4j
public class PublicController {
    private final ScanService scanService;
    private final WebVersionService versionService;
    private final EmailService emailService;

    @Autowired
    public PublicController(ScanService scanService, WebVersionService versionService, EmailService emailService) {
        this.scanService = scanService;
        this.versionService = versionService;
        this.emailService = emailService;
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

    @GetMapping("/version")
    public String showVersionPage(Model model) {
        String readmeContent = versionService.getReadmeContent();
        String changelogContent = versionService.getChangelogContent();

        model.addAttribute("readmeContent", readmeContent);
        model.addAttribute("changelogContent", changelogContent);

        return "public/version";
    }

    @GetMapping("/report")
    public String showReportForm() {
        log.debug("public controller showReportForm");
        return "public/report";
    }

    @PostMapping("/report")
    public String submitReportForm(String email, String subject, String message, Model model) {
        try {
            log.debug("public controller try to send email");
            emailService.sendEmail(email, subject, message);
            return "public/report-success";
        } catch (MessagingException e) {
            log.warn("failed to send email");
            model.addAttribute("errorMessage", "Failed to send email: " + e.getMessage());
            return "public/report-error";
        }
    }

}
