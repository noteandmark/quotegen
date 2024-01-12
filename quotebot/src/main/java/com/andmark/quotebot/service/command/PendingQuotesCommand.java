package com.andmark.quotebot.service.command;

import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.service.ApiService;
import com.andmark.quotebot.service.UserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class PendingQuotesCommand extends QuoteCommand{
    private final ApiService apiService;
    private final UserRoleService userRoleService;

    public PendingQuotesCommand(ApiService apiService, UserRoleService userRoleService) {
        super("pendingquotes", "Receive pending quotes");
        this.apiService = apiService;
        this.userRoleService = userRoleService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {} execute /pendingquotes", user.getId());
        // Check if the user has the ADMIN role
        if (userRoleService.hasRequiredRole(user.getId(), UserRole.ROLE_ADMIN)) {
            log.debug("user with role ADMIN run pending quotes command");
            List<QuoteDTO> pendingQuotes = apiService.getPendingQuotes();
            // sorting the list of citations by date
            pendingQuotes.sort(Comparator.comparing(QuoteDTO::getPendingTime));
            // select the required fields to be displayed to the user
            String formattedQuotes = formatQuoteDTOList(pendingQuotes);
            log.debug("receive pending quotes");
            sendLongMessage(absSender, chat, "Ожидаемые публикации: \n" + formattedQuotes);
        } else {
            sendMessage(absSender, chat, "Эта возможность только для админов");
        }
    }

    private void sendLongMessage(AbsSender absSender, Chat chat, String text) {
        final int MAX_MESSAGE_LENGTH = 4096;
        if (text.length() <= MAX_MESSAGE_LENGTH) {
            sendMessage(absSender, chat, text);
        } else {
            int index = 0;
            while (index < text.length()) {
                int endIndex = Math.min(index + MAX_MESSAGE_LENGTH, text.length());
                String messagePart = text.substring(index, endIndex);
                sendMessage(absSender, chat, messagePart);
                index += MAX_MESSAGE_LENGTH;
            }
        }
    }

    private String formatQuoteDTOList(List<QuoteDTO> quoteDTOList) {
        StringBuilder formattedQuotes = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (QuoteDTO quoteDTO : quoteDTOList) {
            String formattedQuote = String.format("id=%d, pendingTime=%s%n",
                    quoteDTO.getId(), quoteDTO.getPendingTime().format(formatter));
            formattedQuotes.append(formattedQuote);
        }

        return formattedQuotes.toString();
    }


}
