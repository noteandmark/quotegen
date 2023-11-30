package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.UserDTO;
import com.andmark.quotegen.exception.UsernameAlreadyExistsException;
import com.andmark.quotegen.service.RegistrationService;
import com.andmark.quotegen.service.impl.UserServiceImpl;
import com.andmark.quotegen.util.PersonValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final UserServiceImpl userService;
    private final PersonValidator personValidator;
    private final RegistrationService registrationService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserServiceImpl userService, PersonValidator personValidator, RegistrationService registrationService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.personValidator = personValidator;
        this.registrationService = registrationService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, Model model) {
        log.debug("get to /auth/login");
        if (error != null) {
            log.warn("Incorrect login or password entry");
            model.addAttribute("errorMessage", "Неправильный ввод логина или пароля");
        }
        return "auth/login";
    }

    @GetMapping("/registration")
    public String showRegistrationForm(Model model) {
        log.debug("get to /auth/registration");
        model.addAttribute("userDTO", new UserDTO());
        return "auth/registration";
    }

    @PostMapping("/process_login")
    public String processLogin(@Valid @RequestParam String username,
                               @Valid @RequestParam String password,
                               Model model) {
        log.debug("get to /auth/process_login");
        try {
            log.debug("try get authentication with username = {}, password = {}", username, password);

            // Authenticate the user
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Log successful authentication
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            log.debug("Successfully authenticated. User: {}", userDetails.getUsername());

            // Redirect to the home page
            return "redirect:/public/index";
        } catch (AuthenticationException e) {
            log.warn("AuthenticationException with username = {}", username);

            // Handle authentication errors, e.g., incorrect login/password
            model.addAttribute("error", "Invalid username or password");
            return "redirect:/auth/login?error";
        }
    }

    @PostMapping("/registration")
    public String processRegistration(@ModelAttribute("userDTO") @Valid UserDTO userDTO,
                                      Model model,
                                      BindingResult bindingResult,
                                      HttpServletRequest request) {
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
        Long userId = registrationService.registerNewUser(userDTO);
        log.debug("controller: user with id = {}, username = {} registrated", userId, userDTO.getUsername());

        // Authenticate the user after registration
        UserDetails userDetails = userService.loadUserByUsername(userDTO.getUsername());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Redirect to the home page
        return "redirect:/public/index";
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public RedirectView handleUsernameAlreadyExistsException(UsernameAlreadyExistsException e, Model model,
                                                             RedirectAttributes redirectAttributes) {
        log.warn("Exception: {}", e.getMessage());
        //Passing an attribute using Flash Attributes
        redirectAttributes.addFlashAttribute("registrationError", "Пользователь с таким логином уже существует");
        return new RedirectView("/auth/registration",true);
    }

}
