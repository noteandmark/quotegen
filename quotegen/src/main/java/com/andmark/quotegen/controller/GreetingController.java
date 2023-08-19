package com.andmark.quotegen.controller;


import com.andmark.quotegen.service.GreetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/greetings")
@Slf4j
public class GreetingController {
    private final GreetingService greetingService;

    @Autowired
    public GreetingController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/random")
    public ResponseEntity<String> getRandomGreeting() {
        log.debug("greeting controller: getRandomGreeting");
        String randomGreeting = greetingService.getRandomGreeting();
        log.debug("get randomGreeting");
        return ResponseEntity.ok(randomGreeting);
    }
}
