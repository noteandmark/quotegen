//package com.andmark.quotegen.controller.web;
//
//import com.andmark.quotegen.dto.UserDTO;
//import com.andmark.quotegen.service.RegistrationService;
//import com.andmark.quotegen.service.UserService;
//import com.andmark.quotegen.util.PersonValidator;
//import lombok.extern.slf4j.Slf4j;
//import org.modelmapper.ModelMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@Controller
//@RequestMapping("/auth")
//@Slf4j
//public class AuthController {
//
//    private final AuthenticationManager authenticationManager;
//    private final ModelMapper mapper;
//    private final PersonValidator personValidator;
//    private final RegistrationService registrationService;
//    private final UserService userService;
//
//    @Autowired
//    public AuthController(AuthenticationManager authenticationManager, ModelMapper mapper, PersonValidator personValidator, RegistrationService registrationService, UserService userService) {
//        this.authenticationManager = authenticationManager;
//        this.mapper = mapper;
//        this.personValidator = personValidator;
//        this.registrationService = registrationService;
//        this.userService = userService;
//    }
//
//    @GetMapping("/login")
//    public String login(@RequestParam(required = false) String error, Model model) {
//        if (error != null) {
//            model.addAttribute("errorMessage", "Неправильный ввод логина или пароля");
//        }
//        return "auth/login";
//    }
//
//    @GetMapping("/registration")
//    public String registration(@ModelAttribute("userDTO") UserDTO userDTO) {
//        return "auth/registration";
//    }
//
//    @PostMapping("/process_login")
//    public String processLogin(@RequestParam String username,
//                               @RequestParam String password,
//                               Model model,
//                               Authentication authentication) {
//        try {
//            log.debug("try get authentication with username = {}", username);
//
//            // Authenticate the user
//            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            // Log successful authentication
//            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//            log.debug("Successfully authenticated. User: {}", userDetails.getUsername());
//
//            // Check roles
//            boolean isAdmin = userService.isAdmin(authentication);
//            boolean isUser = userService.isUser(authentication);
//            log.debug("Role isAdmin = {}, is User = {}", isAdmin, isUser);
//
//            model.addAttribute("isAdmin", isAdmin);
//            model.addAttribute("isUser", isUser);
//
//            // Redirect to the home page
//            return "redirect:/index";
//        } catch (AuthenticationException e) {
//            log.warn("AuthenticationException with username = {}", username);
//
//            // Handle authentication errors, e.g., incorrect login/password
//            model.addAttribute("error", "Invalid username or password");
//            return "redirect:/auth/login?error";
//        }
//    }
//
//    @PostMapping("/registration")
//    public String processRegistration(@ModelAttribute("userDTO") UserDTO userDTO,
//                                      Model model,
//                                      BindingResult bindingResult) {
//        log.debug("registration post controller");
//
//        // Validate the userDTO
//        personValidator.validate(userDTO, bindingResult);
//
//        log.debug("validate registration");
//        if (bindingResult.hasErrors()) {
//            log.warn("error validation registration");
//            model.addAttribute("registrationError", "Ошибка в процессе регистрации. Попробуйте повторить");
//            return "auth/registration";
//        }
//
//        // Register the new user
//        registrationService.registerNewUser(userDTO);
//        log.debug("controller: user with username = {} registrated", userDTO.getUsername());
//
//        // Redirect to the home page
//        return "redirect:/index";
//    }
//
//}
