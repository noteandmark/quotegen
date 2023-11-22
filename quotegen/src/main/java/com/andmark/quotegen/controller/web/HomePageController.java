package com.andmark.quotegen.controller.web;

import lombok.extern.slf4j.Slf4j;
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

    @RequestMapping({"","/", "/index","/index.html"})
    public String getIndexPage(Model model, Authentication authentication) {
        log.debug("indexPage controller");

        if (authentication != null && authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            boolean isAdmin = authorities.stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

            boolean isUser = authorities.stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER"));

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
