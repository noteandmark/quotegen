package com.andmark.quotebot.service.command;

import com.andmark.quotebot.service.googleapi.GoogleCustomSearchService;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.List;

public class StartCommand extends QuoteCommand {

    public StartCommand() {
        super("start", "Start using the bot");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        sendMessage(absSender, chat, "Welcome to the bot! Use /help to see available commands.");
    }
}
