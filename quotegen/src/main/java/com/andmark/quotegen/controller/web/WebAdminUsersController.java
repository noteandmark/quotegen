package com.andmark.quotegen.controller.web;

import com.andmark.quotegen.dto.UserDTO;
import com.andmark.quotegen.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@Slf4j
public class WebAdminUsersController {
    private final UserService userService;

    public WebAdminUsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showUsers(Model model) {
        log.debug("web admin users controller showGreetings");

        List<UserDTO> users = userService.findAll();
        model.addAttribute("users", users);

        log.debug("return page list of users");
        return "admin/users/list";
    }

    @GetMapping("/view/{id}")
    public String viewUser(@PathVariable("id") Long id, Model model) {
        log.debug("web admin users controller viewUser with id = {}", id);

        UserDTO userDTO = userService.findOne(id);
        if (userDTO != null) {
            log.debug("getting view user");
            model.addAttribute("userDTO", userDTO);
            return "admin/users/view";
        } else {
            log.warn("user with id = {} not found", id);
            return "redirect:/admin/users";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        log.debug("web admin users controller showEditForm");
        UserDTO userDTO = userService.findOne(id);
        model.addAttribute("userDTO", userDTO);
        log.debug("show users-edit page");
        return "admin/users/edit";
    }

    @PostMapping("/edit/{id}")
    public String editUser(@PathVariable("id") Long id, @ModelAttribute("userDTO") UserDTO userDTO) {
        log.debug("web admin users controller editUser with id: {}", userDTO.getId());
        UserDTO foundUserDTO = userService.findOne(userDTO.getId());
        log.debug("found user and set password for possible update role");
        userDTO.setPassword(foundUserDTO.getPassword());
        userService.update(userDTO);
        log.debug("web admin users controller: updated, go to redirect");
        return "redirect:/admin/users";
    }

}
