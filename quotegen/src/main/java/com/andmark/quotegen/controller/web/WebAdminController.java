package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.domain.enums.QuoteStatus;
import com.andmark.quotegen.dto.QuoteDTO;
import com.andmark.quotegen.exception.NotFoundBookException;
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
import java.util.ArrayList;
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

    @GetMapping("/requestquote")
    public String requestQuote(Model model) {
        log.debug("Admin Controller: Request Quote");

        try {
            quoteService.checkAndPopulateCache();
            QuoteDTO quoteDTO = quoteService.provideQuoteToClient();
            String content = quoteDTO.getContent();
            String truncatedContent = content.substring(0, Math.min(content.length(), 1024));

            List<String> imageUrls = new ArrayList<>();
            if (!content.isEmpty()) {
                imageUrls = googleCustomSearchService.searchImagesByKeywords(truncatedContent);
                log.debug("Found {} images", imageUrls.size());
            }

            model.addAttribute("quote", quoteDTO);
            model.addAttribute("imageUrls", imageUrls);
            model.addAttribute("selectedImageNumber", 0);
            log.debug("return admin/requestquote");

            return "admin/requestquote";
        } catch (NotFoundBookException e) {
            // Handle the case when there are no books
            log.error(e.getMessage());
            model.addAttribute("exception", e.getMessage());
            return "public/404error";
        }

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
        log.debug("selectedImageUrl " + (selectedImageUrl != "-1" ? "=" + selectedImageUrl : "is null"));
        pendingQuote.setImageUrl("-1".equals(selectedImageUrl) ? null : selectedImageUrl);

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

        // Set attribute to indicate confirmation
        redirectAttributes.addFlashAttribute("confirmation", true);
        // Add the quote as a flash attribute (saved between requests) to RedirectAttributes
        redirectAttributes.addFlashAttribute("quote", pendingQuote);

        // Redirect to success page
        log.debug("redirect to success html");
        return "redirect:/admin/success";
    }

    @PostMapping("/rejectquote")
    public String rejectQuote(@RequestParam Long quoteId, RedirectAttributes redirectAttributes) {
        log.debug("web admin controller rejectQuote");

        quoteService.rejectQuote(quoteId);

        // Set attributes to indicate deletion
        redirectAttributes.addFlashAttribute("deletion", true);
        redirectAttributes.addFlashAttribute("deletedQuoteId", quoteId);

        return "redirect:/admin/success";
    }

    @GetMapping("/success")
    public String successPage() {
        return "admin/success";
    }

}

//            List<String> imageUrls = List.of(
//                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRq3Qu9hdaxRk66j5he0jMCm0Iqz-3lvEryFO8Yxr91dNDroTjC9B0cYw&s",
//                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTgsEduSV5RgaT36tDc-Pb_h-voTsNexfFPT35nX2Keu2b8bIwZZtbMDRw&s",
//                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTGiENqPYh_elPDhTS58Y9YwN95pS9F_akwCDEmiTSlMKnV2O0wG7mQlg&s",
//                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT_yqyadOdnemlLb6am3sDcoGCIoKxgNsF9Oc8DDFxndafReIlG4YJwdg&s",
//                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSzQLu_AJ9zY_V9hEuo2XA27Vtvbyi67ud1yF95i-dgqoUUROO65kyBjQE&s",
//                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSt8RRuulTm1e3YiAV3ixW3I0zW12DKIlVvlQQQHDasH7dtl-jTKKYtsg&s",
//                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQVvHAL0rwhCmtiQZJIuft7jr3AcvziPw4sLGqsJ12BmOk2NVx_U8n6lQ&s",
//                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRqeZxq9aC2qwo2yv9NatbHgL0dku2p-vT0ERctmeNL_fGfArysneQnQA&s",
//                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSWrRr8OlLORes8-pwE0s41w7Kc2iWZcfzQsqLK_R10pQaTpytb90QYWA&s",
//                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSbCPGafo5vo51hqNq-iOsF5gm5H4-Cxry4gJiAatl19H9rLO1eQPMrCQo&"
//            );