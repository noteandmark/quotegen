package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.BookDTO;
import com.andmark.quotegen.dto.QuoteDTO;
import com.andmark.quotegen.service.BookService;
import com.andmark.quotegen.service.QuoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/quote")
@Slf4j
public class WebAdminQuoteController {
    private final QuoteService quoteService;
    private final BookService bookService;

    public WebAdminQuoteController(QuoteService quoteService, BookService bookService) {
        this.quoteService = quoteService;
        this.bookService = bookService;
    }

    @GetMapping
    public String showQuotes(Model model, @PageableDefault(size = 20) Pageable pageable) {
        log.debug("web admin quote controller showQuotes");
        Page<QuoteDTO> quotesPage = quoteService.findAll(pageable);

        // use getContent to get the list of elements on the current page
        model.addAttribute("quotes", quotesPage.getContent());
        model.addAttribute("page", quotesPage);

        // Add page size to the model
//        int pageSize = pageable.getPageSize();
//        log.debug("web admin quote controller showQuotes with page size = {}", pageSize);
//        model.addAttribute("pageSize", pageSize);

        return "admin/quote/list";
    }

    @GetMapping("/view/{id}")
    public String viewQuote(@PathVariable("id") Long id, Model model) {
        log.debug("web admin quote controller viewQuote with id = {}", id);
        QuoteDTO quoteDTO = quoteService.findOne(id);
        if (quoteDTO != null) {
            model.addAttribute("quoteDTO", quoteDTO);
            if (quoteDTO.getBookId() != null) {
                BookDTO bookDTO = bookService.findOne(quoteDTO.getBookId());
                model.addAttribute("bookDTO", bookDTO);
            }
            return "admin/quote/view";
        } else {
            log.warn("quote with id = {} not found", id);
            return "redirect:/admin/quote";
        }
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        log.debug("web admin quote controller showCreateForm");
        QuoteDTO quoteDTO = new QuoteDTO();
        model.addAttribute("quoteDTO", quoteDTO);
        return "admin/quote/create";
    }

    @PostMapping("/create")
    public String createQuote(@ModelAttribute("quoteDTO") QuoteDTO quoteDTO) {
        log.debug("web admin quote controller createQuote");
        quoteService.save(quoteDTO);
        return "redirect:/admin/quote";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        log.debug("web admin quote controller showEditForm");
        QuoteDTO quoteDTO = quoteService.findOne(id);
        model.addAttribute("quoteDTO", quoteDTO);

        // add the book data
        if (quoteDTO != null && quoteDTO.getBookId() != null) {
            BookDTO bookDTO = bookService.findOne(quoteDTO.getBookId());
            model.addAttribute("bookDTO", bookDTO);
        }

        return "admin/quote/edit";
    }

    @PostMapping("/edit/{id}")
    public String editQuote(@PathVariable("id") Long id, @ModelAttribute("quoteDTO") QuoteDTO quoteDTO) {
        log.debug("web admin quote controller editQuote");
        quoteService.update(id, quoteDTO);
        log.debug("web admin quote controller: updated, go to redirect");
        return "redirect:/admin/quote";
    }

    @GetMapping("/delete/{id}")
    public String deleteQuote(@PathVariable("id") Long id) {
        log.debug("web admin quote controller deleteQuote");
        quoteService.delete(id);
        return "redirect:/admin/quote";
    }
}
