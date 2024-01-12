package com.andmark.quotebot.service.command;

import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.service.ApiService;
import com.andmark.quotebot.service.enums.BotState;
import com.andmark.quotebot.util.BotAttributes;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
public class SuggestQuoteCommand extends QuoteCommand{
    private final ApiService apiService;

    public SuggestQuoteCommand(ApiService apiService) {
        super("suggestquote", "suggest quote");
        this.apiService = apiService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {} execute /suggestquote", user.getId());

        // Check if the user has the USER or ADMIN role
        UserRole userRole = apiService.getUserRole(user.getId());
        log.debug("user role = {} for user.getId() = {}", userRole, user.getId());

        if (userRole == UserRole.ROLE_ADMIN || userRole == UserRole.ROLE_USER) {
            log.debug("request for quote content");
            sendMessage(absSender, chat, "Введите текст цитаты, предлагаемой к публикации в группе");
            // Set the next step for the user's
            Long usertgId = user.getId();
            BotAttributes.setUserCurrentBotState(usertgId, BotState.AWAITING_QUOTE_CONTENT);
        } else {
            sendMessage(absSender, chat, "Эта возможность только для зарегистрированных пользователей. Воспользуйтесь командой /signup");
        }

    }
}
