package com.andmark.quotegen.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@Slf4j
public class HomePageController {

    @RequestMapping({"","/", "/index", "/public/index"})
    public String getIndexPage(Model model) {
        log.debug("indexPage controller");
        return "public/index";
    }

}
