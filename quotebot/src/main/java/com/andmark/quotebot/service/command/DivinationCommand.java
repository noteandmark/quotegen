package com.andmark.quotebot.service.command;

import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.service.ApiService;
import com.andmark.quotebot.util.BotAttributes;
import com.andmark.quotebot.service.enums.BotState;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DivinationCommand extends QuoteCommand {
    private final ApiService apiService;

    // Keep track of users who have already used the command today
    static final Map<Long, LocalDate> usersLastUsage = new ConcurrentHashMap<>();

    public DivinationCommand(ApiService apiService) {
        super("divination", "Request a book divination");
        this.apiService = apiService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());

        // Check if the user has the ADMIN role
        UserRole userRole = apiService.getUserRole(user.getId());
        log.debug("user role = {} for user.getId() = {}", userRole, user.getId());

        if (userRole == UserRole.ADMIN || userRole == UserRole.USER) {

            String messageText;
            if (arguments.length == 0) {
                log.debug("arguments is null");
                messageText = "Добро пожаловать в \"Гадание по книге\":\n" +
                        "Загадали вопрос?\n" +
                        "Я выберу наугад книгу и верну вам отрывок по заданным странице и строке.\n" +
                        "Правда, без редактирования там может быть всё, что угодно... Попробуем?\n" +
                        "Один раз в день можем сыграть в такую игру ;)\n" +
                        "Напишите номер страницы:";
            } else {
                log.debug("argument has value: {}", arguments[0]);
                usersLastUsage.put(user.getId(), null);
                messageText = arguments[0];
            }

            if (!hasUserUsedCommandToday(user.getId())) {
                message.setText(messageText);
                // Set the next step for the user's input
                BotAttributes.setUserCurrentBotState(user.getId(), BotState.AWAITING_PAGE_NUMBER);

                // Mark the user as having used the command today
                markUserUsage(user.getId());
            } else {
                message.setText("Вы уже использовали команду \"Гадание по книге\" сегодня. Попробуйте завтра снова.");
            }
        } else {
            message.setText("Эта возможность только для зарегистрированных пользователей. Воспользуйтесь командой /signup");
        }
        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            log.warn("TelegramApiException is = {}", e.getMessage());
        }
    }

    private boolean hasUserUsedCommandToday(Long userId) {
        LocalDate lastUsage = usersLastUsage.get(userId);
        return lastUsage != null && lastUsage.equals(LocalDate.now());
    }

    private void markUserUsage(Long userId) {
        usersLastUsage.put(userId, LocalDate.now());
    }

}