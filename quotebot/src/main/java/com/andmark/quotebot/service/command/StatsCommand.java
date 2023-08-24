package com.andmark.quotebot.service.command;

import com.andmark.quotebot.dto.StatsDTO;
import com.andmark.quotebot.service.ApiService;
import lombok.extern.slf4j.Slf4j;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
public class StatsCommand extends QuoteCommand {
    private final ApiService apiService;

    public StatsCommand(ApiService apiService) {
        super("stats", "Different statistics");
        this.apiService = apiService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {} execute /stats", user.getId());
        try {
            // Make a request to the API to get the statistics
            StatsDTO stats = apiService.getStats();
            // Format and send the statistics message
            String statsMessage = String.format(
                    "Статистика:\n" +
                            "Количество книг в каталоге: %d\n" +
                            "Количество опубликованных Книголюбом цитат за год: %d\n" +
                            "Количество ожидаемых к публикации цитат: %d",
                    stats.getBookCount(), stats.getPublishedQuotesThisYear(), stats.getPendingQuotesCount()
            );
            sendMessage(absSender, chat, statsMessage);
        } catch (Exception e) {
            log.error("Error while retrieving statistics: {}", e.getMessage());
            sendMessage(absSender, chat, "An error occurred while retrieving statistics.");
        }
    }
}
