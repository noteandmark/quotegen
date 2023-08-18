package com.andmark.quotebot.service.command;

import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.service.ApiService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
public class RequestQuoteCommand extends QuoteCommand {

    private final ApiService apiService;

    public RequestQuoteCommand(ApiService apiService) {
        super("requestquote", "Request a new quote");
        this.apiService = apiService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {} execute /requestquote", user.getId());
        // Check if the user has the ADMIN role
        UserRole userRole = apiService.getUserRole(user.getId());
        log.debug("user role = {} for user.getId() = {}", userRole, user.getId());

        if (userRole == UserRole.ADMIN) {
        // Make a request to the REST API to get the next quote
            log.debug("user with role ADMIN run request quote command");
            apiService.getNextQuote();
        } else {
            sendMessage(absSender,chat,"Эта возможность только для админов");
        }
    }
}
