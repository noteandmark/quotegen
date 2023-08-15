package com.andmark.quotegen.service;

import com.andmark.quotegen.domain.enums.UserRole;
import com.andmark.quotegen.dto.UserDTO;

public interface UserService extends AbstractService<UserDTO>{
    boolean isRegistered(Long id);
    boolean existsByUsername(String username);

    UserRole getUserRole(Long usertgId);
}
