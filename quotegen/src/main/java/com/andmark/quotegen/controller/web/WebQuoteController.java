package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.QuoteDTO;
import com.andmark.quotegen.service.QuoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

}
