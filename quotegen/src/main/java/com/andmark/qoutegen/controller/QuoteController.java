package com.andmark.qoutegen.controller;

import com.andmark.qoutegen.dto.QuoteDTO;
import com.andmark.qoutegen.service.QuoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
@Slf4j
public class QuoteController {
    private final QuoteService quoteService;

    @Autowired
    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping("/get-next")
    public ResponseEntity<QuoteDTO> getNextQuote() {
        log.debug("controller getNextQuote");
        quoteService.checkAndPopulateCache();
        QuoteDTO quoteDTO = quoteService.provideQuoteToClient();
        log.info("response with quote = {}", quoteDTO);
        return ResponseEntity.ok(quoteDTO);
    }

    @GetMapping("/all")
    public ResponseEntity<List<QuoteDTO>> getAllQuotes() {
        log.debug("controller getAllQuotes");

        List<QuoteDTO> quotes = quoteService.findAll();
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuoteDTO> getQuoteById(@PathVariable Long id) {
        log.debug("controller getQuoteById id = {}", id);

        QuoteDTO quoteDTO = quoteService.findOne(id);
        if (quoteDTO != null) {
            return ResponseEntity.ok(quoteDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuote(@PathVariable Long id) {
        log.debug("controller deleteQuote id = {}", id);

        QuoteDTO existingQuote = quoteService.findOne(id);
        if (existingQuote != null) {
            quoteService.delete(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/confirm")
//    public ResponseEntity<Void> confirmQuote(@RequestParam Long id, @RequestParam String content) {
    public ResponseEntity<Void> confirmQuote(@RequestBody QuoteDTO quoteDTO) {
        log.debug("controller confirmQuote id = {}", quoteDTO.getId());
        quoteService.confirmQuote(quoteDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reject")
    public ResponseEntity<Void> rejectQuote(@RequestParam Long id) {
        log.debug("controller rejectQuote id = {}", id);
        QuoteDTO existingQuote = quoteService.findOne(id);

        if (existingQuote != null) {
            log.debug("controller reject quote id = {}", id);
            quoteService.delete(id);
            log.info("controller delete quote id = {}", id);
            return ResponseEntity.ok().build();
        } else {
            log.warn("controller not find quote with id = {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/generate")
    public ResponseEntity<Void> generateQuotes(@RequestParam(name = "count", required = false) Integer count) {
        int cacheSize = (count != null && count > 0) ? count : 30;
        log.debug("Controller generating quotes in quantity = {}", cacheSize);
        //overload protection
        if (cacheSize < 300)
            quoteService.populateCache(cacheSize);
        return ResponseEntity.ok().build();
    }

}
