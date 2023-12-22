package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.GreetingDTO;
import com.andmark.quotegen.service.GreetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/greeting")
@Slf4j
public class WebAdminGreetingController {
    private final GreetingService greetingService;

    @Autowired
    public WebAdminGreetingController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping
    public String showGreetings(Model model) {
        log.debug("web admin greetings controller showGreetings");

        List<GreetingDTO> greetings = greetingService.findAll();
        model.addAttribute("greetings", greetings);

        log.debug("return page list of greetings");
        return "admin/greeting/list";
    }

    @GetMapping("/view/{id}")
    public String viewGreeting(@PathVariable("id") Long id, Model model) {
        log.debug("web admin greetings controller viewGreeting with id = {}", id);

        GreetingDTO greeting = greetingService.findOne(id);
        if (greeting != null) {
            model.addAttribute("greeting", greeting);
            return "admin/greeting/view";
        } else {
            log.warn("greeting with id = {} not found", id);
            return "redirect:/admin/greeting";
        }
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        log.debug("web admin greeting controller showCreateForm");
        GreetingDTO greeting = new GreetingDTO();
        model.addAttribute("greeting", greeting);
        return "admin/greeting/create";
    }

    @PostMapping("/create")
    public String createGreeting(@ModelAttribute("greeting") GreetingDTO greeting) {
        log.debug("web admin greeting controller createGreeting");
        greetingService.save(greeting);
        return "redirect:/admin/greeting";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        log.debug("web admin greeting controller showEditForm");
        GreetingDTO greeting = greetingService.findOne(id);
        model.addAttribute("greeting", greeting);
        log.debug("show greeting-edit page");
        return "admin/greeting/edit";
    }

    @PostMapping("/edit/{id}")
    public String editGreeting(@PathVariable("id") Long id, @ModelAttribute("greeting") GreetingDTO greeting) {
        log.debug("web admin greeting controller editGreeting with id: {}", greeting.getId());
        greetingService.update(greeting);
        log.debug("web admin greeting controller: updated, go to redirect");
        return "redirect:/admin/greeting";
    }

    @GetMapping("/delete/{id}")
    public String deleteGreeting(@PathVariable("id") Long id) {
        log.debug("web admin greeting controller deleteGreeting");
        greetingService.delete(id);
        return "redirect:/admin/greeting";
    }

}
