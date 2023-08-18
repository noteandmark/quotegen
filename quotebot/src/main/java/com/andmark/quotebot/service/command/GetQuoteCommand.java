package com.andmark.quotebot.service.command;

import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.service.ApiService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
public class GetQuoteCommand extends QuoteCommand{
    private final ApiService apiService;

    public GetQuoteCommand(ApiService apiService) {
        super("getquote", "Show quote from previously published");
        this.apiService = apiService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {} execute /getquote", user.getId());
        // Check if the user has the ADMIN role
        UserRole userRole = apiService.getUserRole(user.getId());
        log.debug("user role = {} for user.getId() = {}", userRole, user.getId());

        if (userRole == UserRole.ADMIN || userRole == UserRole.USER) {
            // Make a request to the quotegen API to get a random published quote
            QuoteDTO randomQuote = apiService.getRandomPublishedQuote();

            if (randomQuote != null) {
                // Send the received quote to the user
                String message = "Случайная цитата из опубликованных:\n" +
                        randomQuote.getContent();
                sendMessage(absSender, chat, message);
            } else {
                // If null is returned, no published quotes were found
                sendMessage(absSender, chat, "К сожалению, или нет опубликованных цитат, или какая-то ошибка на сервере.");
            }
        } else {
            sendMessage(absSender,chat,"Эта возможность только для зарегистрированных пользователей. Воспользуйтесь командой /signup");
        }
    }
}
