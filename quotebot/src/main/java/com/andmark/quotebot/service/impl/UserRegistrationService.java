package com.andmark.quotebot.service.impl;

import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.dto.UserDTO;
import com.andmark.quotebot.service.Bot;
import com.andmark.quotebot.service.BotAttributes;
import com.andmark.quotebot.service.UserService;
import com.andmark.quotebot.service.enums.BotState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

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
        if (usersInProgress.contains(usertgId) && (botAttributes.getCurrentState() == BotState.AWAITING_USERNAME_INPUT)) {
            log.warn("User registration is already in progress for user: {}", usertgId);
            return;
        }
        // Mark user as in progress
        usersInProgress.add(usertgId);
        botAttributes.setCurrentState(BotState.AWAITING_USERNAME_INPUT);
        telegramBot.sendMessage(chatId, null, "Введите имя (логин) пользователя");
    }

    public void handleUsernameInput(Long usertgId, Long chatId, String username) {
        log.debug("handleUsernameInput in UserRegistrationService");
        if (usersInProgress.contains(usertgId)) {
            log.debug("contains usertgId = {}", usertgId);
            telegramBot.sendMessage(chatId, null, "Имя пользователя принято. Введите пароль.");
            botAttributes.setUsername(username);
            botAttributes.setCurrentState(BotState.AWAITING_PASSWORD_INPUT);
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
            userDTO.setPassword(password);
            userDTO.setRole(UserRole.USER);
        }
        return userDTO;
    }

    public void completeRegistration(Long usertgId, Long chatId) {
        log.info("success registration");
        usersInProgress.remove(usertgId);
        botAttributes.setUsername(null);
        telegramBot.sendMessage(chatId, null, "Вы зарегистрированы. Теперь можете пользоваться командами бота");
    }

}