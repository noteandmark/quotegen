package com.andmark.quotebot.service.command;

import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.service.ApiService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class ScanBooksCommand extends QuoteCommand {
    private final ApiService apiService;

    public ScanBooksCommand(ApiService apiService) {
        super("scanbooks", "Scan books in a directory");
        this.apiService = apiService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        // Check if the user has the ADMIN role
        UserRole userRole = apiService.getUserRole(user.getId());
        log.debug("user role = {} for user.getId() = {}", userRole, user.getId());

        if (userRole == UserRole.ADMIN) {
            log.debug("user with role ADMIN run scan books command");

            SendMessage message = new SendMessage();
            message.setChatId(chat.getId().toString());

            if (arguments == null || arguments.length == 0) {
                // Prompt the user for the directory path
                message.setText("Please enter the directory path to scan books:");
            } else {
                // Send a request to your REST API to scan books
                String directoryPath = String.join(" ", arguments);
                String text = apiService.scanBooks(directoryPath);
                message.setText(text);
            }
            try {
                absSender.execute(message);
            } catch (TelegramApiException e) {
                // Handle exception
                log.error("TelegramApiException in ScanBooksCommand");
            }
        } else {
            sendMessage(absSender, chat, "Эта возможность только для админов");

        }
    }
}
