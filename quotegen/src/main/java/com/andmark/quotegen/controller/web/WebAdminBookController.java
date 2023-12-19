package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.BookDTO;
import com.andmark.quotegen.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/book")
@Slf4j
public class WebAdminBookController {
    private final BookService bookService;

    @Autowired
    public WebAdminBookController(BookService bookService) {
        this.bookService = bookService;
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
        bookService.update(id, bookDTO);

        // перенаправляем обратно на страницу редактирования цитаты
        if (quoteId != null && !quoteId.isEmpty()) {
            return "redirect:" + quoteId;
        } else {
            return "redirect:/admin/quote";
        }
    }

}
