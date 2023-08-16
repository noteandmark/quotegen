package com.andmark.quotebot.service.command;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
public class QuotesWeekCommand extends QuoteCommand{

    public QuotesWeekCommand() {
        super("quotes_for_week", "Showing quotes for the week");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {} execute /quote_for_week", user.getId());
        sendMessage(absSender, chat, "Эта возможность запланирована в следующих версиях программы");
    }
}