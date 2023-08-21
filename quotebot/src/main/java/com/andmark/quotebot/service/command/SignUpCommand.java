package com.andmark.quotebot.service.command;

import com.andmark.quotebot.util.BotAttributes;
import com.andmark.quotebot.service.UserService;
import com.andmark.quotebot.service.enums.BotState;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
public class SignUpCommand extends QuoteCommand{
    private final UserService userService;
    private final BotAttributes botAttributes;

    public SignUpCommand(UserService userService, BotAttributes botAttributes) {
        super("signup", "Register a new user");
        this.userService = userService;
        this.botAttributes = botAttributes;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {} execute /signup", user.getId());
        // Check if the user is already registered
        Long usertgId = user.getId();
        if (userService.isRegistered(usertgId)) {
            // User is already registered
            // Send a message indicating that they are already registered
            sendMessage(absSender, chat, "Вы уже зарегистрированы");
            return;
        }
        // Initiate the registration process
        BotAttributes.setUserCurrentBotState(user.getId(), BotState.START);
        userService.initiateRegistration(chat.getId(), usertgId);
    }
}
