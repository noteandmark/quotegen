package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.StatsDTO;
import com.andmark.quotegen.service.EmailService;
import com.andmark.quotegen.service.ScanService;
import com.andmark.quotegen.service.WebVersionService;
import com.andmark.quotegen.util.TimeProvider;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.andmark.quotegen.config.AppConfig.MIN_TIME_THRESHOLD;

@Controller
@RequestMapping("/public")
@Slf4j
public class PublicController {
    private final ScanService scanService;
    private final WebVersionService versionService;
    private final EmailService emailService;
    private final TimeProvider timeProvider;

    @Autowired
    public PublicController(ScanService scanService, WebVersionService versionService, EmailService emailService, TimeProvider timeProvider) {
        this.scanService = scanService;
        this.versionService = versionService;
        this.emailService = emailService;
        this.timeProvider = timeProvider;
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
        String privatePolicyContent = versionService.getPrivatePolicyContent();

        model.addAttribute("readmeContent", readmeContent);
        model.addAttribute("changelogContent", changelogContent);
        model.addAttribute("privatePolicyContent", privatePolicyContent);

        return "public/version";
    }

    @GetMapping("/report")
    public String showReportForm() {
        log.debug("public controller showReportForm");
        return "public/report";
    }

    @PostMapping("/report")
    public String submitReportForm(String email, String subject, String message, String timeStamp, Model model) {
        long currentTime = timeProvider.getCurrentTimeMillis();
        long formSubmitTime = Long.parseLong(timeStamp);

        // checking the time of form submission
        if (currentTime - formSubmitTime < MIN_TIME_THRESHOLD) {
            // reject form submission because too little time has passed
            model.addAttribute("errorMessage", "Форма отправлена слишком быстро. Возможна активность бота.");
            return "public/report-error";
        }

        try {
            log.debug("public controller try to send email");
            emailService.sendEmail(email, subject, message);
            model.addAttribute("successMessage", "Ваше сообщение было успешно отправлено.");
            return "public/report-success";
        } catch (MessagingException e) {
            log.warn("failed to send email");
            model.addAttribute("errorMessage", "Failed to send email: " + e.getMessage());
            return "public/report-error";
        }
    }

}
