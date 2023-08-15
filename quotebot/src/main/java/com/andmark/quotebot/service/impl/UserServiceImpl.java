package com.andmark.quotebot.service.impl;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.dto.UserDTO;
import com.andmark.quotebot.service.ApiService;
import com.andmark.quotebot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRegistrationService userRegistrationService;
    private final ApiService apiService;

    public UserServiceImpl(UserRegistrationService userRegistrationService, ApiService apiService) {
        this.userRegistrationService = userRegistrationService;
        this.apiService = apiService;
    }

    @Override
    public void initiateRegistration(Long usertgId, Long chatId) {
        userRegistrationService.initiateRegistration(usertgId, chatId);
    }

    @Override
    public boolean isRegistered(Long usertgId) {
        return apiService.existsByUsertgId(usertgId);
    }

    @Override
    public boolean isUsernameTaken(String username) {
        return apiService.existsByUsername(username);
    }

}
