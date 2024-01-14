package com.andmark.quotegen.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        log.error("An unexpected error occurred", e);
        String errorMessage = "An unexpected error occurred";
        model.addAttribute("errorMessage", errorMessage);
        return "public/error";
    }

}
