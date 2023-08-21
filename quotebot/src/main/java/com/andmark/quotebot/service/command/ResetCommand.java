package com.andmark.quotebot.service.command;

import com.andmark.quotebot.util.BotAttributes;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
public class ResetCommand extends QuoteCommand {
    private final BotAttributes botAttributes;

    public ResetCommand(BotAttributes botAttributes) {
        super("reset", "Reset settings");
        this.botAttributes = botAttributes;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {} execute /reset", user.getId());
        botAttributes.clear(user.getId());
        sendMessage(absSender, chat, "Настройки сброшены до 'по умолчанию'");
    }
}
