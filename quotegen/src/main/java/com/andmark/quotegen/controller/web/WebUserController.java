package com.andmark.quotegen.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web")
@Slf4j
public class WebUserController {

    @GetMapping("/divination")
    public String divination() {
        log.debug("web controller divination");
        return "web/divination";
    }

}
