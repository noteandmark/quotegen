package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.UserDTO;
import com.andmark.quotegen.exception.ServiceException;
import com.andmark.quotegen.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web")
@Slf4j
public class WebProfileController {

    private final UserService userService;

    public WebProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile/{username}")
    public String userProfile(@PathVariable String username, Model model) {
        // Получите данные пользователя по username и передайте их в модель
        UserDTO userDTO = userService.findByUsername(username);
        model.addAttribute("user", userDTO);
        return "web/profile";
    }

    @PostMapping("/profile/{username}")
    public String updateNickname(@PathVariable String username, @ModelAttribute("user") UserDTO updatedUser,
                                 Model model) {
        try {
            userService.update(updatedUser);
            return "redirect:/web/profile/" + username;
        } catch (ServiceException e) {
            log.warn("ServiceException in updateNickname");
            model.addAttribute("errorMessage", e.getMessage());
            return "public/error";
        }
    }

    @GetMapping("/change-password/{username}")
    public String changePasswordForm(@PathVariable String username, Model model) {
        // Передаем имя пользователя в модель для отображения на странице
        log.debug("webProfileController changePasswordForm with username = {}", username);
        model.addAttribute("username", username);
        return "web/change-password";
    }

    @PostMapping("/change-password/{username}")
    public String changePassword(@PathVariable String username,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.changePassword(username, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully");
        } catch (IllegalArgumentException e) {
            log.warn("IllegalArgumentException in changePassword");
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/web/change-password/" + username;
    }

}
