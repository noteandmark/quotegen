package com.andmark.quotebot.service.impl;

import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.dto.UserDTO;
import com.andmark.quotebot.service.Bot;
import com.andmark.quotebot.util.BotAttributes;
import com.andmark.quotebot.service.enums.BotState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class UserRegistrationService {
    private final Set<Long> usersInProgress = new HashSet<>();
    private final Bot telegramBot;
    private final BotAttributes botAttributes;

    public UserRegistrationService(@Lazy Bot telegramBot, BotAttributes botAttributes) {
        this.telegramBot = telegramBot;
        this.botAttributes = botAttributes;
    }

    public void initiateRegistration(Long usertgId, Long chatId) {
        if (usersInProgress.contains(usertgId) && (BotAttributes.getUserCurrentBotState(usertgId) == BotState.AWAITING_USERNAME_INPUT)) {
            log.warn("User registration is already in progress for user: {}", usertgId);
            return;
        }
        // Mark user as in progress
        usersInProgress.add(usertgId);
        BotAttributes.setUserCurrentBotState(usertgId, BotState.AWAITING_USERNAME_INPUT);
        telegramBot.sendMessage(chatId, null, "Введите имя (логин) пользователя");
    }

    public void handleUsernameInput(Long usertgId, Long chatId, String username) {
        log.debug("handleUsernameInput in UserRegistrationService");
        if (usersInProgress.contains(usertgId)) {
            log.debug("contains usertgId = {}", usertgId);
            telegramBot.sendMessage(chatId, null, "Имя пользователя принято. Введите пароль.");
            botAttributes.setUsername(username);
        }
    }

    public UserDTO handlePasswordInput(Long usertgId, Long chatId, String password) {
        log.debug("handlePasswordInput in UserRegistrationService");
        UserDTO userDTO = null;
        if (usersInProgress.contains(usertgId)) {
            log.debug("contains usertgId = {}", usertgId);
            // Notify the user that the password is accepted
            telegramBot.sendMessage(chatId, null, "Пароль принят.");
            // Complete the registration process by creating the user's information in DTO
            String username = botAttributes.getUsername();
            userDTO = new UserDTO();
            userDTO.setUsertgId(usertgId);
            userDTO.setUsername(username);
            //nickname = username by default
            userDTO.setNickname(username);
            userDTO.setPassword(password);
            userDTO.setRole(UserRole.USER);
        }
        return userDTO;
    }

    public boolean isUserInProgress(Long userId) {
        return usersInProgress.contains(userId);
    }

    public void setUsersInProgress(Long userId) {
        usersInProgress.add(userId);
    }

    public void completeRegistration(Long usertgId, Long chatId) {
        log.info("success registration");
        usersInProgress.remove(usertgId);
        log.debug("removed usertgId from usersInProgress");
        botAttributes.setUsername(null);
        telegramBot.sendMessage(chatId, null, "Вы зарегистрированы. Теперь можете пользоваться командами бота");
    }

}
