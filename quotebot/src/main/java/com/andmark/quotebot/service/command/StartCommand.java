package com.andmark.quotebot.service.command;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
public class StartCommand extends QuoteCommand {

    public StartCommand() {
        super("start", "Start using the bot");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {} execute /start", user.getId());
        sendMessage(absSender, chat, "Welcome to the bot! Use /help to see available commands.");
    }
}
