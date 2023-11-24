package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

@Controller
@Slf4j
public class HomePageController {

    private final UserService userService;

    @Autowired
    public HomePageController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping({"","/", "/index","/index.html"})
    public String getIndexPage(Model model, Authentication authentication) {
        log.debug("indexPage controller");

        if (userService.isAuthenticated(authentication)) {
            log.debug("log in with authentication");
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            boolean isAdmin = userService.isAdmin(authentication);
            boolean isUser = userService.isUser(authentication);

            log.debug("role isAdmin = {}, is User = {}", isAdmin, isUser);
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("isUser", isUser);
        }

        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

}
