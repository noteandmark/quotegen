package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.domain.enums.QuoteStatus;
import com.andmark.quotegen.dto.QuoteDTO;
import com.andmark.quotegen.service.GoogleCustomSearchService;
import com.andmark.quotegen.service.QuoteService;
import com.andmark.quotegen.service.WebAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
@Slf4j
public class WebAdminController {
    private final QuoteService quoteService;
    private final WebAdminService webAdminService;
    private final GoogleCustomSearchService googleCustomSearchService;

    @Autowired
    public WebAdminController(QuoteService quoteService, WebAdminService webAdminService, GoogleCustomSearchService googleCustomSearchService) {
        this.quoteService = quoteService;
        this.webAdminService = webAdminService;
        this.googleCustomSearchService = googleCustomSearchService;
    }

//    public String acceptQuote(@RequestParam Long quoteId,
//                              @RequestParam String publishOption,
//                              @RequestParam(required = false) String publishDate,
//                              @RequestParam String quoteContent,
//                              @RequestParam String bookAuthor,
//                              @RequestParam String bookTitle,
//                              RedirectAttributes redirectAttributes) {

//        QuoteDTO pendingQuote = QuoteDTO.builder()
//                .id(quoteId)
//                .content(quoteContent)
//                .status(QuoteStatus.PENDING)
//                .imageUrl(null) //TODO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//                .bookAuthor(bookAuthor)
//                .bookTitle(bookTitle)
//                .build();

    @GetMapping("/requestquote")
    public String requestQuote(Model model) {
        log.debug("Admin Controller: Request Quote");

        QuoteDTO quoteDTO = quoteService.provideQuoteToClient();
        String content = quoteDTO.getContent();
        String truncatedContent = content.substring(0, Math.min(content.length(), 1024));

        List<String> imageUrls = googleCustomSearchService.searchImagesByKeywords(truncatedContent);

        log.debug("Found {} images", imageUrls.size());
        log.debug("imageUrls = {}", imageUrls);
        log.debug("---");

        model.addAttribute("quote", quoteDTO);
        model.addAttribute("imageUrls", imageUrls);
        model.addAttribute("selectedImageNumber", 0);

        return "admin/requestquote";
    }

    @PostMapping("/acceptquote")
    public String acceptQuote(@ModelAttribute("quote") QuoteDTO pendingQuote,
                              @RequestParam String publishOption,
                              @RequestParam(required = false) String publishDate,
                              @RequestParam(required = false) String selectedImageUrl,
                              RedirectAttributes redirectAttributes) {
        log.debug("web admin controller acceptQuote");

        pendingQuote.setStatus(QuoteStatus.PENDING);

        // Set the imageUrl in quoteDTO based on selectedImageUrl
        log.debug("selectedImageUrl " + (selectedImageUrl != null ? "=" + selectedImageUrl : "is null"));
        pendingQuote.setImageUrl("0".equals(selectedImageUrl) ? null : selectedImageUrl);

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
