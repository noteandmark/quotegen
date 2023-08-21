package com.andmark.quotebot.service.command;

import com.andmark.quotebot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
public class SignOutCommand extends QuoteCommand{
    private final UserService userService;

    public SignOutCommand(UserService userService) {
        super("signout", "Reset user and password");
        this.userService = userService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {} execute /signout", user.getId());
        Long usertgId = user.getId();
        if (userService.isRegistered(usertgId)) {
            // Delete user's record and initiate re-registration
            userService.deleteUser(chat.getId(), usertgId);
            // Initiate registration process
            userService.initiateRegistration(chat.getId(), usertgId);
        } else {
            // User is not registered, inform them to register first
            sendMessage(absSender, chat, "Вы еще не зарегистрированы. Используйте команду меню /signout");
        }
    }
}
