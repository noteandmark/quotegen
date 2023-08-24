package com.andmark.quotebot.service.command;

import com.andmark.quotebot.service.enums.BotState;
import com.andmark.quotebot.util.BotAttributes;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.andmark.quotebot.config.BotConfig.adminChatId;

@Slf4j
public class ReportCommand extends QuoteCommand {

    public ReportCommand() {
        super("report", "report message to admin");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("report command: ");
        SendMessage message = new SendMessage();

        log.debug("report command: strings.length < 1");
        message.setChatId(chat.getId().toString());
        message.setText("Отправьте мне сообщение, я его переправлю админу.");
        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            log.warn("TelegramApiException is = {}", e.getMessage());
        }
        BotAttributes.setUserCurrentBotState(user.getId(), BotState.AWAITING_REPORT);
    }
}
