package com.andmark.quotebot.service.command;

import com.andmark.quotebot.service.ApiService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
public class GetQuoteCommand extends QuoteCommand{

    public GetQuoteCommand(ApiService apiService) {
        super("getquote", "Show quote from previously published");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {} execute /getquote", user.getId());
        sendMessage(absSender, chat, "Эта возможность запланирована в следующих версиях программы");
    }
}
