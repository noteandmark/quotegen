package com.andmark.quotebot.service.command;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class QuotesWeekCommand extends QuoteCommand{

    public QuotesWeekCommand() {
        super("quotes_for_week", "Showing quotes for the week");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        sendMessage(absSender, chat, "Эта возможность запланирована в следующих версиях программы");
    }
}
