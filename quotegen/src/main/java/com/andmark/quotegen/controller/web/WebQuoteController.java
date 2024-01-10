package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.QuoteDTO;
import com.andmark.quotegen.service.QuoteService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/web")
@Slf4j
public class WebQuoteController {
    private final QuoteService quoteService;

    public WebQuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping("/getquote")
    public String getRandomQuote(Model model) {
        log.debug("web quote controller getRandomQuote");
        QuoteDTO randomQuote = quoteService.getRandomPublishedQuote();

        if (randomQuote != null) {
            log.debug("add randomQuote with id = {}", randomQuote.getId());
            model.addAttribute("quote", randomQuote);
        } else {
            log.warn("no published quotes");
            model.addAttribute("errorMessage", "К сожалению, нет опубликованных цитат.");
        }

        return "web/quote";
    }

    @GetMapping("/quotes-for-week")
    public String getQuotesForWeek(Model model) {
        log.debug("web quote controller getQuotesForWeek");
        List<QuoteDTO> quotes = quoteService.getPublishedQuotesForWeek();

        model.addAttribute("quotes", quotes);

        return "web/quotes-for-week";
    }

    @GetMapping("/suggest-quote")
    public String showSuggestQuotePage(Model model) {
        log.debug("web quote controller showSuggestQuotePage");
        QuoteDTO quoteDTO = new QuoteDTO();
        model.addAttribute("quoteForm", quoteDTO);
        return "web/suggest-quote";
    }

    @PostMapping("/suggest-quote")
    public String suggestQuote(@ModelAttribute("quoteForm") @Valid QuoteDTO quoteDTO,
                               BindingResult result, Authentication authentication,
                               Model model) {
        if (result.hasErrors()) {
            log.warn("post suggestQuote has errors");
            return "web/suggest-quote";
        }

        if (quoteDTO.getImageUrl() != null && quoteDTO.getImageUrl().trim().isEmpty()) {
            quoteDTO.setImageUrl(null);
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        log.debug("suggestQuote from user name: {}", username);

        quoteService.suggestQuote(quoteDTO, username);

        model.addAttribute("successMessage", "Ваша цитата добавлена в раздел предлагаемых к публикации! Спасибо!");
        return "public/report-success";
    }

}
