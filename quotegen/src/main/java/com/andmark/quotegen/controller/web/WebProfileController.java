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
        if (userDTO == null) {
            log.warn("userDTO with username = {} does not exists", username);
            model.addAttribute("errorMessage", "Пользователь с именем " + username + " не найден.");
            return "public/error";
        }
        model.addAttribute("user", userDTO);
        return "web/profile";
    }

    @PostMapping("/profile/{username}")
    public String updateNickname(@PathVariable String username, @ModelAttribute("user") UserDTO updatedUser,
                                 Model model) {
        try {
            log.debug("controller updateNickname");
            userService.updateNickname(updatedUser);
            System.out.println("get to redirect");
            return "redirect:/web/profile/" + username;
        } catch (ServiceException e) {
            System.out.println("Exception!");
            log.warn("ServiceException in updateNickname");
            model.addAttribute("errorMessage", e.getMessage());
            return "public/error";
        }
    }

    @GetMapping("/change-password/{username}")
    public String changePasswordForm(@PathVariable String username, Model model) {
        // pass the username to the model to be displayed on the page
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
            log.debug("webProfileController changePassword");
            userService.changePassword(username, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully");
        } catch (IllegalArgumentException e) {
            log.warn("IllegalArgumentException in changePassword");
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/web/change-password/" + username;
    }

    @PostMapping("/profile/delete/{username}")
    public String deleteUser(@PathVariable String username) {
        try {
            log.debug("webProfileController deleteUser with username = {}", username);
            userService.deleteByUsername(username);
            log.debug("redirect to signout");
            return "redirect:/signout";
        } catch (ServiceException e) {
            log.warn("ServiceException in deleteUser");
            return "redirect:/web/profile/" + username + "?error=" + e.getMessage();
        }
    }

}
