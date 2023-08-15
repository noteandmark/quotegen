package com.andmark.quotebot.service.command;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class StatsCommand extends QuoteCommand{

    public StatsCommand() {
        super("stats", "Different statistics");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        sendMessage(absSender, chat, "Эта возможность запланирована в следующих версиях программы");
    }

}
