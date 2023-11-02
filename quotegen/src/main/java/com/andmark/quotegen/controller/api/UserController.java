package com.andmark.quotegen.controller.api;

import com.andmark.quotegen.domain.enums.UserRole;
import com.andmark.quotegen.dto.UserDTO;
import com.andmark.quotegen.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/exists/{usertgId}")
    public ResponseEntity<Boolean> isUserRegistered(@PathVariable long usertgId) {
        log.debug("user controller in isUserRegistered");
        boolean isRegistered = userService.isRegistered(usertgId);
        log.info("user with usertgId = {} isRegistered = {}", usertgId, isRegistered);
        return ResponseEntity.ok(isRegistered);
    }

    @GetMapping("/username-taken/{username}")
    public ResponseEntity<Boolean> isUsernameTaken(@PathVariable String username) {
        log.debug("user controller in isUsernameTaken");
        boolean isTaken = userService.existsByUsername(username);
        return ResponseEntity.ok(isTaken);
    }

    @GetMapping("/get-role/{usertgId}")
    public ResponseEntity<UserRole> getUserRole(@PathVariable long usertgId) {
        log.debug("user controller in getUserRole");
        UserRole userRole = userService.getUserRole(usertgId);
        log.debug("user role is {}", userRole);
        return ResponseEntity.ok(userRole);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) {
        log.debug("user controller in registerUser");
        try {
            userService.save(userDTO);
            log.info("controller success save user, return response");
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            log.error("user controller exception save user : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error registering user");
        }
    }

    @DeleteMapping("/delete/{usertgId}")
    public ResponseEntity<Void> deleteUser(@PathVariable long usertgId) {
        log.debug("user controller in deleteUser");
        userService.delete(usertgId);
        log.debug("user controller: user deleted");
        return ResponseEntity.ok().build();
    }
}
