package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.BookDTO;
import com.andmark.quotegen.dto.QuoteDTO;
import com.andmark.quotegen.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/book")
@Slf4j
public class WebAdminBookController {
    private final BookService bookService;

    @Autowired
    public WebAdminBookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String showBooks(Model model) {
        log.debug("web admin book controller showBooks");

        List<BookDTO> books = bookService.findAll();
        model.addAttribute("books", books);

        log.debug("return page list of books");
        return "admin/book/list";
    }

    @GetMapping("/view/{id}")
    public String viewBook(@PathVariable("id") Long id, Model model) {
        log.debug("web admin book controller viewQuote with id = {}", id);
        BookDTO bookDTO = bookService.findOne(id);
        if (bookDTO != null) {
            model.addAttribute("bookDTO", bookDTO);
            return "admin/book/view";
        } else {
            log.warn("book with id = {} not found", id);
            return "redirect:/admin/book";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditBookForm(@PathVariable("id") Long id,
                                   @RequestParam(name = "quoteId", required = false) String quoteId,
                                   Model model) {
        log.debug("web admin book controller showEditBookForm");
        BookDTO bookDTO = bookService.findOne(id);
        model.addAttribute("bookDTO", bookDTO);

        // передаем id страницы цитаты в модель, если пришли из окна редактирования цитаты
        log.debug("quoteId = {}", quoteId);
        model.addAttribute("quoteId", quoteId);

        log.debug("show book-edit page");
        return "admin/book/edit";
    }

    @PostMapping("/edit/{id}")
    public String editBook(@PathVariable("id") Long id,
                           @ModelAttribute("bookDTO") BookDTO bookDTO,
                           @RequestParam(name = "quoteId", required = false) String quoteId) {
        log.debug("web admin book controller editBook");
        bookService.update(bookDTO);

        // redirect back to the quote view page
        if (quoteId != null && !quoteId.isEmpty()) {
            return "redirect:/admin/quote/view/" + quoteId;
        } else {
            return "redirect:/admin/quote";
        }
    }

}
