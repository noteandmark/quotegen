package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.AvailableDayResponseDTO;
import com.andmark.quotegen.dto.QuoteDTO;
import com.andmark.quotegen.service.QuoteService;
import com.andmark.quotegen.service.WebAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin")
@Slf4j
public class WebAdminController {
    private final QuoteService quoteService;
    private final WebAdminService webAdminService;

    @Autowired
    public WebAdminController(QuoteService quoteService, WebAdminService webAdminService) {
        this.quoteService = quoteService;
        this.webAdminService = webAdminService;
    }

    @GetMapping("/requestquote")
    public String requestQuote(Model model) {
        log.debug("admin controller requestquote");
        QuoteDTO quoteDTO = quoteService.provideQuoteToClient();
        model.addAttribute("quote", quoteDTO);
        return "admin/requestquote";
    }

    @PostMapping("/acceptquote")
    public String acceptQuote(@RequestParam Long quoteId,
                              @RequestParam String publishOption,
                              @RequestParam(required = false) String publishDate,
                              @RequestParam String quoteContent,
                              @RequestParam String bookAuthor,
                              @RequestParam String bookTitle,
                              Model model) {
        log.debug("web admin controller acceptQuote");

        QuoteDTO pendingQuote = QuoteDTO.builder()
                .id(quoteId)
                .content(quoteContent)
                .imageUrl(null) //TODO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                .bookAuthor(bookAuthor)
                .bookTitle(bookTitle)
                .build();

        // Process accept action based on the selected publish option
        switch (publishOption) {
            case "random":
                log.debug("case random");
                webAdminService.randomPublish(pendingQuote);
                break;
            case "now":
                // Publish the quote immediately
//                quoteService.publishNow(quoteId);
                break;
            case "chosen":
                // Publish the quote on the selected date
//                quoteService.publishOnDate(quoteId, publishDate);
                break;
            default:
                log.warn("Unsupported publish option: {}", publishOption);
                // Handle the case when an unsupported option is selected
                break;
        }

        // Redirect to success page
        return "redirect:/admin/success";
    }

    @PostMapping("/rejectquote")
    public String rejectQuote(@RequestParam Long quoteId, Model model) {
        // Implement logic to reject the quote (e.g., delete from the database)
        // ...

        // Redirect to success page
        return "redirect:/admin/success";
    }

    @GetMapping("/success")
    public String successPage() {
        return "admin/success";
    }

}
