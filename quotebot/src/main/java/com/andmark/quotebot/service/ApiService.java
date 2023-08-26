package com.andmark.quotebot.service;

import com.andmark.quotebot.domain.RequestConfiguration;
import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.dto.*;

import java.util.List;

public interface ApiService {
    QuoteDTO getNextQuote();

    List<QuoteDTO> getPendingQuotes();

    boolean existsByUsertgId(Long id);

    boolean existsByUsername(String username);

    void registerUser(Long chatId, UserDTO userDTO);

    UserRole getUserRole(Long usertgId);

    void sendRequestAndHandleResponse(RequestConfiguration requestConfig);

    void deleteUser(Long chatId, Long usertgId);

    String getResponseYesOrNo(String apiUrl);

    StatsDTO getStats();

    QuoteDTO getRandomPublishedQuote();

    List<QuoteDTO> getWeekPublishedQuotes();

    String getRandomGreeting();

    String scanBooks(String directoryPath);

    ScheduledActionStatusDTO getScheduledActionStatus();

    void updateScheduledActionStatus(ScheduledActionStatusDTO now);

    ExtractedLinesDTO processPageAndLineNumber(Long userId, int pageNumber, int lineNumber);

    byte[] downloadImage(String imageUrl);
}
