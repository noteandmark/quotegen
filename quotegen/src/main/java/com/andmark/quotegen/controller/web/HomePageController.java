package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.UserDTO;
import com.andmark.quotegen.service.RegistrationService;
import com.andmark.quotegen.service.UserService;
import com.andmark.quotegen.service.impl.UserServiceImpl;
import com.andmark.quotegen.util.PersonValidator;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@Slf4j
public class HomePageController {

    private final UserServiceImpl userService;
    private final PersonValidator personValidator;
    private final RegistrationService registrationService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public HomePageController(UserServiceImpl userService, PersonValidator personValidator, RegistrationService registrationService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.personValidator = personValidator;
        this.registrationService = registrationService;
        this.authenticationManager = authenticationManager;
    }

    @RequestMapping({"","/", "/index", "/public/index"})
    public String getIndexPage(Model model) {
        log.debug("indexPage controller");
        log.debug("SecurityContextHolder.getContext().getAuthentication() = {}", SecurityContextHolder.getContext().getAuthentication());

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
////        Authentication authentication = authenticationManager.
//        if (authentication.getPrincipal() instanceof UserDetails) {
//            log.debug("authentication instanceof UserDetails");
//            log.debug("authentication = {}", authentication);
//
//            if (userService.isAuthenticated(authentication)) {
//                log.debug("log in with authentication");
//
//                boolean isAdmin = userService.isAdmin(authentication);
//                boolean isUser = userService.isUser(authentication);
//
//                log.debug("role isAdmin = {}, is User = {}", isAdmin, isUser);
//                model.addAttribute("isAdmin", isAdmin);
//                model.addAttribute("isUser", isUser);
//            }
//        }
//        else {
//            log.debug("authentication instanceof UserDetails");
//            log.debug("authentication is {}", authentication);
//        }

        log.debug("return index page");
        return "public/index";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/auth/login")
    public String login(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Неправильный ввод логина или пароля");
        }
        return "auth/login";
    }

    @GetMapping("/auth/registration")
    public String registration(@ModelAttribute("userDTO") UserDTO userDTO) {
        return "auth/registration";
    }

    @PostMapping("/auth/process_login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               Model model,
                               HttpSession session) {
        try {
            log.debug("try get authentication with username = {}, password = {}", username, password);

            // Authenticate the user
//            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
//            SecurityContextHolder.getContext().setAuthentication(authentication);

//            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
//                    username, password);
//            Authentication authentication = this.authenticationManager.authenticate(token);
//            SecurityContext context = SecurityContextHolder.createEmptyContext();
//            context.setAuthentication(authentication);
//            SecurityContextHolder.setContext(context);

            UserDetails principal = userService.loadUserByUsername(username);
            Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);

//            Authentication authentication  =
//                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

//            Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
//            Authentication authentication = this.authenticationManager.authenticate(authenticationRequest);

            // Log successful authentication
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            log.debug("Successfully authenticated. User: {}", userDetails.getUsername());

            // Check roles
            boolean isAdmin = userService.isAdmin(authentication);
            boolean isUser = userService.isUser(authentication);
            log.debug("Role isAdmin = {}, is User = {}", isAdmin, isUser);

            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("isUser", isUser);

            // Set authentication to session manually
            log.debug("before SecurityContextHolder.getContext().getAuthentication() = {}", SecurityContextHolder.getContext().getAuthentication());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("after SecurityContextHolder.getContext().getAuthentication() = {}", SecurityContextHolder.getContext().getAuthentication());

            // Redirect to the home page
            return "redirect:/public/index";
        } catch (AuthenticationException e) {
            log.warn("AuthenticationException with username = {}", username);

            // Handle authentication errors, e.g., incorrect login/password
            model.addAttribute("error", "Invalid username or password");
            return "redirect:/auth/login?error";
        }
    }

    @PostMapping("/auth/registration")
    public String processRegistration(@ModelAttribute("userDTO") UserDTO userDTO,
                                      Model model,
                                      BindingResult bindingResult) {
        log.debug("registration post controller");

        // Validate the userDTO
        personValidator.validate(userDTO, bindingResult);

        log.debug("validate registration");
        if (bindingResult.hasErrors()) {
            log.warn("error validation registration");
            model.addAttribute("registrationError", "Ошибка в процессе регистрации. Попробуйте повторить");
            return "auth/registration";
        }

        // Register the new user
        registrationService.registerNewUser(userDTO);
        log.debug("controller: user with username = {} registrated", userDTO.getUsername());

        // Redirect to the home page
        return "redirect:/public/index";
    }

}
