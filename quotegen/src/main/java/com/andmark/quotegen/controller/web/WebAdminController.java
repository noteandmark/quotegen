package com.andmark.quotegen.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@Slf4j
public class WebAdminController {

    @GetMapping("/requestquote")
    public String requestQuote() {
        log.debug("admin controller requestquote");
        return "admin/requestquote";
    }

}
