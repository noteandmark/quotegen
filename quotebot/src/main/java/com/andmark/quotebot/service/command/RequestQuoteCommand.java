package com.andmark.quotebot.service.command;

import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.service.ApiService;
import com.andmark.quotebot.service.UserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
public class RequestQuoteCommand extends QuoteCommand {

    private final ApiService apiService;
    private final UserRoleService userRoleService;

    public RequestQuoteCommand(ApiService apiService, UserRoleService userRoleService) {
        super("requestquote", "Request a new quote");
        this.apiService = apiService;
        this.userRoleService = userRoleService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {} execute /requestquote", user.getId());

        // Check if the user has the ADMIN role
        if (userRoleService.hasRequiredRole(user.getId(), UserRole.ROLE_ADMIN)) {
            log.debug("user with role ADMIN run request quote command");
            apiService.getNextQuote();
        } else {
            sendMessage(absSender, chat, "Эта возможность только для админов");
        }
    }
}
