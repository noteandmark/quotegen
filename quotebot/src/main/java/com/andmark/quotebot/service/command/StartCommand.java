package com.andmark.quotebot.service.command;

import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.service.ApiService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
public class StartCommand extends QuoteCommand {
    private final ApiService apiService;

    public StartCommand(ApiService apiService) {
        super("start", "Start using the bot");
        this.apiService = apiService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {}, name = {} execute /start", user.getId(), user.getUserName());
        UserRole userRole = apiService.getUserRole(user.getId());
        String justMessage = (userRole != UserRole.ADMIN || userRole != UserRole.USER) ?
                "Зарегистрируйся, чтобы получить доступ ко всем :)" :
                "Приветствую, " + user.getUserName();

        sendMessage(absSender, chat, "Добро пожаловать! Меня зовут бот Книголюб. Я в сети.\n"
                + "Используй /help для просмотра всех возможностей.\n"
                + justMessage);
    }
}
