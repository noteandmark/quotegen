package com.andmark.quotebot.service;

import com.andmark.quotebot.domain.RequestConfiguration;
import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.dto.StatsDTO;
import com.andmark.quotebot.dto.UserDTO;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

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
}
