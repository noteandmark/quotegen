package com.andmark.quotebot.util;

import com.andmark.quotebot.service.enums.BotState;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Slf4j
public class BotAttributes {

    private Long quoteId;
    private int lastMessageId;
    private List<String> imageUrls;
    private String lastCallbackMessage;
    private String confirmedContent;
    private String confirmedUrl;
    private String username;
    private static final Map<Long, BotState> userStates = new ConcurrentHashMap<>();
    private static final Map<Long, UserSession> userSessions = new ConcurrentHashMap<>();

    public static BotState getUserCurrentBotState(Long userId) {
        return userStates.getOrDefault(userId, BotState.START);
    }

    public static void setUserCurrentBotState(Long userId, BotState state) {
        userStates.put(userId, state);
    }

    public void setPageNumber(long userId, int pageNumber) {
        getUserSession(userId).setPageNumber(pageNumber);
    }

    public int getPageNumber(long userId) {
        return getUserSession(userId).getPageNumber();
    }

    public void setLineNumber(long userId, int lineNumber) {
        getUserSession(userId).setLineNumber(lineNumber);
    }

    public int getLineNumber(long userId) {
        return getUserSession(userId).getLineNumber();
    }

    public static void clear(long userId) {
        log.debug("clear botAttributes for userId = {}", userId);
        userSessions.remove(userId);
        userStates.remove(userId);
    }

    private UserSession getUserSession(long userId) {
        return userSessions.computeIfAbsent(userId, k -> new UserSession());
    }


    private static class UserSession {
        private int pageNumber;
        private int lineNumber;

        public int getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }
    }
}
