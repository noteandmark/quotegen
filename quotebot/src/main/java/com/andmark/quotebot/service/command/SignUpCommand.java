package com.andmark.quotebot.service.command;

import com.andmark.quotebot.service.UserService;
import com.andmark.quotebot.service.impl.UserRegistrationService;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class SignUpCommand extends QuoteCommand{
    private final UserService userService;

    public SignUpCommand(UserService userService) {
        super("signup", "Register a new user");
        this.userService = userService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        // Check if the user is already registered
        Long usertgId = user.getId();
        if (userService.isRegistered(usertgId)) {
            // User is already registered
            // Send a message indicating that they are already registered
            sendMessage(absSender, chat, "Вы уже зарегистрированы");
            return;
        }
        // Initiate the registration process
        userService.initiateRegistration(usertgId, chat.getId());

        // Send a message to start the registration flow
        sendMessage(absSender, chat, "Введите имя (логин) пользователя");
    }
}
