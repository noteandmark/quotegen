package com.andmark.quotebot.service.command;

import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.service.ApiService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
public class WebLinkCommand extends QuoteCommand{
    private final ApiService apiService;

    public WebLinkCommand(ApiService apiService) {
        super("weblink", "Receive web link");
        this.apiService = apiService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {} execute /weblink", user.getId());
        // Check if the user has the USER or ADMIN role
        UserRole userRole = apiService.getUserRole(user.getId());
        log.debug("user role = {} for user.getId() = {}", userRole, user.getId());

        if (userRole == UserRole.ROLE_ADMIN || userRole == UserRole.ROLE_USER) {
            log.debug("user run weblink command");
            String webLink = apiService.getWebLink();
            log.debug("receive web link, send to the user");
            String message = (webLink != null) ? "По этой ссылке вы можете перейти на веб-версию программы:\n" + webLink :
                    "Произошла ошибка в получении ссылки на веб-версию. Попробуйте позже или сообщите админу с помощью команды /report";
            sendMessage(absSender, chat, message);
        } else {
            sendMessage(absSender, chat, "Эта возможность только для админов");
        }
    }

}
