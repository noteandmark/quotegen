package com.andmark.quotegen.service;

import com.andmark.quotegen.domain.User;
import com.andmark.quotegen.domain.enums.UserRole;
import com.andmark.quotegen.dto.UserDTO;
import org.springframework.transaction.annotation.Transactional;

public interface UserService extends AbstractService<UserDTO>{
    boolean isRegistered(Long id);
    boolean existsByUsername(String username);
    UserDTO findByUsername(String username);

    UserRole getUserRole(Long usertgId);

    void changePassword(String username, String currentPassword, String newPassword);

    UserDTO findOneByUsertgId(Long usertgId);

    @Transactional
    void deleteByUsertgId(Long id);

    void deleteByUsername(String username);

    User convertToEntity(UserDTO userDTO);
}
