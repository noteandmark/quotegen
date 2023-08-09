package com.andmark.quotebot.service.command;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class StartCommand extends BotCommand {

    public StartCommand() {
        super("start", "Start using the bot");
    }
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        SendMessage response = new SendMessage();
        response.setChatId(chat.getId().toString());
        response.setText("Welcome to the bot! Use /help to see available commands.");
        try {
            absSender.execute(response);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
