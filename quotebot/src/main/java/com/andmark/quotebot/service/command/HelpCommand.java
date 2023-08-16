package com.andmark.quotebot.service.command;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
public class HelpCommand extends QuoteCommand{

    public HelpCommand() {
        super("help", "Get information about available commands");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {} execute /help", user.getId());
        StringBuilder message = new StringBuilder();
        message.append("Добро пожаловать! Книголюб приветствует!\n\n");
        message.append("У меня есть такое меню команд:\n");
        message.append("/start - запустить бот\n");
        message.append("/help - отобразить справочную информацию\n");
        message.append("/signup - зарегистрироваться\n");
        message.append("/da_net - да или нет\n");
        message.append("/quotes_for_week - цитаты за неделю\n");
        message.append("/stats - разная статистика\n");
        message.append("/version - версия и список новшеств\n");
        message.append("/requestquote - сгенерировать цитату (админ)\n");

        sendMessage(absSender, chat, message.toString());
    }
}
