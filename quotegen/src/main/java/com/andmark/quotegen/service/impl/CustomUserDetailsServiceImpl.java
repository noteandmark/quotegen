package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.dto.UserDTO;
import com.andmark.quotegen.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("in method loadUserByUsername find username: {}", username);

        UserDTO userDTO = userService.findByUsername(username);

        if (userDTO == null) {
            log.debug("user not found with username: {}", username);
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return User.builder()
                .username(userDTO.getUsername())
                .password(userDTO.getPassword())
                .authorities(userDTO.getRole().name()) // Добавление роли как SimpleGrantedAuthority
                .disabled(false)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }

}
