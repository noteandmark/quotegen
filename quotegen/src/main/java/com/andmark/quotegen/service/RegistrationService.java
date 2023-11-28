package com.andmark.quotegen.service;

import com.andmark.quotegen.dto.UserDTO;

public interface RegistrationService {
    Long registerNewUser(UserDTO userDTO);
}
