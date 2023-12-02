package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.QuoteDTO;
import com.andmark.quotegen.service.QuoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@Slf4j
public class WebAdminController {
    private final QuoteService quoteService;

    @Autowired
    public WebAdminController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping("/requestquote")
    public String requestQuote(Model model) {
        log.debug("admin controller requestquote");
        QuoteDTO quoteDTO = quoteService.provideQuoteToClient();
        model.addAttribute("quote", quoteDTO);
        return "admin/requestquote";
    }

    @PostMapping("/acceptquote")
    public String acceptQuote(@RequestParam Long quoteId, Model model) {
        // Implement logic to accept the quote (e.g., set status to PUBLISHED)
        // ...

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
