package com.andmark.quotegen.service;

import com.andmark.quotegen.domain.enums.UserRole;
import com.andmark.quotegen.dto.UserDTO;
import org.springframework.security.core.Authentication;

public interface UserService extends AbstractService<UserDTO>{
    boolean isRegistered(Long id);
    boolean existsByUsername(String username);
    UserDTO findByUsername(String username);

    UserRole getUserRole(Long usertgId);

    boolean isAuthenticated(Authentication authentication);

    boolean isAdmin(Authentication authentication);

    boolean isUser(Authentication authentication);
}
