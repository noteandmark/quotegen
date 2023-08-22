package com.andmark.quotebot.service.command;

import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.service.ApiService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.List;

@Slf4j
public class QuotesWeekCommand extends QuoteCommand {
    private final ApiService apiService;

    public QuotesWeekCommand(ApiService apiService) {
        super("quotes_for_week", "Showing quotes for the week");
        this.apiService = apiService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {} execute /quote_for_week", user.getId());
        // Check if the user has the ADMIN role
        UserRole userRole = apiService.getUserRole(user.getId());
        log.debug("user role = {} for user.getId() = {}", userRole, user.getId());

        if (userRole == UserRole.ADMIN || userRole == UserRole.USER) {
            // Make a request to the quotegen API to get a random published quote
            List<QuoteDTO> quotesForWeek = apiService.getWeekPublishedQuotes();

            if (quotesForWeek != null && !quotesForWeek.isEmpty()) {
                log.debug("quotesForWeek is not null, sending quotes");
                for (QuoteDTO quoteDTO : quotesForWeek) {
                    sendMessage(absSender, chat, quoteDTO.getContent());
                }
            } else {
                // If null is returned, no published quotes were found
                sendMessage(absSender, chat, "К сожалению, или нет опубликованных цитат за неделю, или какая-то ошибка на сервере.");
            }
        } else {
            sendMessage(absSender, chat, "Эта возможность только для зарегистрированных пользователей. Воспользуйтесь командой /signup");
        }
    }

}
