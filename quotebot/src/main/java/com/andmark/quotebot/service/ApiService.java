package com.andmark.quotebot.service;

import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.dto.UserDTO;
import org.springframework.http.HttpMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

public interface ApiService {
    QuoteDTO getNextQuote();
    List<QuoteDTO> getPendingQuotes();
    boolean existsByUsertgId(Long id);
    boolean existsByUsername(String username);

    void registerUser(UserDTO userDTO);

    UserRole getUserRole(Long usertgId);

    void sendRequestAndHandleResponse(String rejectUrl, HttpMethod delete, Object o, String text, InlineKeyboardMarkup keyboard);
}
