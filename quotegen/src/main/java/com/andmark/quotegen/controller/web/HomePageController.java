package com.andmark.quotegen.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class HomePageController {

    @RequestMapping({"","/", "/index","/index.html"})
    public String welcomePage() {
        log.debug("welcomePage controller");
        return "index";
    }


}
