package com.andmark.quotebot.service.command;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class YesNoMagicCommand extends QuoteCommand{

    public YesNoMagicCommand() {
        super("da_net", "Ask a question and get an answer in the form of a gif");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        sendMessage(absSender, chat, "Эта возможность запланирована в следующих версиях программы");
    }
}
