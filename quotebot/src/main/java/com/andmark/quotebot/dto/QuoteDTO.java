package com.andmark.quotebot.dto;

import com.andmark.quotebot.domain.enums.QuoteStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuoteDTO {
    private Long id;
    private String content;
    private QuoteStatus status;
    private LocalDateTime pendingTime;
    private String imageUrl;
    private LocalDateTime usedAt;

    private Long bookId;
    private String bookAuthor;
    private String bookTitle;

    // Static factory method to create QuoteDTO instance with error message
    public static QuoteDTO createErrorMessage(String errorMessage) {
        return new QuoteDTO(null, errorMessage, null, null, null,null, null, null, null);
    }
}
