package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.ExtractedLinesDTO;
import com.andmark.quotegen.dto.PageLineRequestDTO;
import com.andmark.quotegen.service.BookService;
import com.andmark.quotegen.service.WebDivinationService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/web")
@Slf4j
public class WebDivinationController {
    private final BookService bookService;
    private final WebDivinationService webDivinationService;

    public WebDivinationController(BookService bookService, WebDivinationService webDivinationService) {
        this.bookService = bookService;
        this.webDivinationService = webDivinationService;
    }

    @PostMapping("/divination")
    public String performDivination(@RequestParam int pageNumber,
                                    @RequestParam int lineNumber,
                                    Model model, HttpSession session, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            //check if the user performed guessing in this session
            boolean performedDivination = webDivinationService.checkPerformedDivination(session);

            if (!performedDivination) {
                log.debug("Not performed divination in this session for user: {}", username);
                PageLineRequestDTO requestDTO = new PageLineRequestDTO(pageNumber, lineNumber);
                ExtractedLinesDTO extractedLinesDTO = bookService.processPageAndLineNumber(requestDTO);

                model.addAttribute("extractedLines", extractedLinesDTO.getLines());
                model.addAttribute("bookAuthor", extractedLinesDTO.getBookAuthor());
                model.addAttribute("bookTitle", extractedLinesDTO.getBookTitle());

                webDivinationService.markUserAsPerformedDivinationSession(session);
            } else {
                log.debug("Already performed divination in this session for user: {}", username);
                String message = "Вы уже делали гадание сегодня. Попробуйте завтра снова.";
                model.addAttribute("message", message);
            }
        } else {
            log.error("Authentication or Principal is null");
            // handle the case when authentication or principal is null
        }

        return "web/divination-result";
    }
}
