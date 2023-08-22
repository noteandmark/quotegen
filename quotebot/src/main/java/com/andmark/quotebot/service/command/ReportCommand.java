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

//        if (strings.length < 1) {
            log.debug("report command: strings.length < 1");
            message.setChatId(chat.getId().toString());
            message.setText("Отправьте мне сообщение для отправки и вновь нажмите эту команду.");
            try {
                absSender.execute(message);
            } catch (TelegramApiException e) {
                log.warn("TelegramApiException is = {}", e.getMessage());
            }
            BotAttributes.setUserCurrentBotState(user.getId(), BotState.AWAITING_REPORT);
//            return;
//        }

//        String reportMessage = String.join(" ", strings);
//        message.setChatId(adminChatId);
//        log.info("new report from user id = {}, username = {}, message = {}", user.getId(), user.getUserName(), reportMessage);
//        message.setText("Новое сообщение от пользователя @" + user.getUserName() + ":\n" + reportMessage);
//        try {
//            absSender.execute(message);
//            log.info("Report sent to admin.");
//        } catch (TelegramApiException e) {
//            log.error("Error sending report to admin: {}", e.getMessage());
//        }
    }
}
