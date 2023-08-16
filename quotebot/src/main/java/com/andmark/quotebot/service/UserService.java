package com.andmark.quotebot.service;

import com.andmark.quotebot.domain.enums.UserRole;

public interface UserService {
    boolean isRegistered(Long usertgId);

    boolean isUsernameTaken(String username);

    void initiateRegistration(Long chatId, Long usertgId);

    void deleteUser(Long chatId, Long usertgId);
}
