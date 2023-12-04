package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.domain.enums.QuoteStatus;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
                              RedirectAttributes redirectAttributes) {
        log.debug("web admin controller acceptQuote");

        QuoteDTO pendingQuote = QuoteDTO.builder()
                .id(quoteId)
                .content(quoteContent)
                .status(QuoteStatus.PENDING)
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
            case "chosen":
                // Publish the quote on the selected date
                log.debug("case chosen with publishDate = {}", publishDate);
                pendingQuote.setPendingTime(LocalDateTime.parse(publishDate));
                webAdminService.chosenPublish(pendingQuote);
                break;
            default:
                log.warn("Unsupported publish option: {}", publishOption);
                // Handle the case when an unsupported option is selected
                break;
        }

        // Add the quote as a flash attribute (saved between requests) to RedirectAttributes
        redirectAttributes.addFlashAttribute("quote", pendingQuote);

        // Redirect to success page
        log.debug("redirect to success html");
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
