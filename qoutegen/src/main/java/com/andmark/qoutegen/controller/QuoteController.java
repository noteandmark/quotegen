package com.andmark.qoutegen.controller;

import com.andmark.qoutegen.domain.Quote;
import com.andmark.qoutegen.dto.QuoteDTO;
import com.andmark.qoutegen.service.QuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {
    private final QuoteService quoteService;

    @Autowired
    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping("/get-next")
    public ResponseEntity<String> getNextQuote() {

        quoteService.checkAndPopulateCache();
//        quoteService.waitForSuitableQuotes();
        String quoteText = quoteService.provideQuoteToClient();

        return ResponseEntity.ok(quoteText);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Quote>> getAllQuotes() {
        List<Quote> quotes = quoteService.findAll();
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quote> getQuoteById(@PathVariable Long id) {
        Quote quote = quoteService.findOne(id);
        if (quote != null) {
            return ResponseEntity.ok(quote);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuote(@PathVariable Long id) {
        Quote existingQuote = quoteService.findOne(id);
        if (existingQuote != null) {
            quoteService.delete(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmQuote(@RequestParam Long id) {
        quoteService.confirmQuote(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject")
    public ResponseEntity<Void> rejectQuote(@RequestParam Long id) {
        quoteService.deleteQuote(id);
        return ResponseEntity.ok().build();
    }
}
