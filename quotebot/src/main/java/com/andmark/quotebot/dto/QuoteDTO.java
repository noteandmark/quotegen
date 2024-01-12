package com.andmark.quotebot.dto;

import com.andmark.quotebot.domain.enums.QuoteStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuoteDTO {
    private Long id;
    @NotBlank(message = "Цитата не должна быть пустой")
    private String content;
    private QuoteStatus status;
    private LocalDateTime pendingTime;
    private String imageUrl;
    private LocalDateTime usedAt;

    private Long bookId;
    @NotBlank(message = "Название книги не должно быть пустым")
    private String bookAuthor;
    @NotBlank(message = "Автор не должен быть пустым")
    private String bookTitle;

    private Long userId;

    // Static factory method to create QuoteDTO instance with error message
    public static QuoteDTO createErrorMessage(String errorMessage) {
        return new QuoteDTO(null, errorMessage, null, null, null,null,null, null, null, null);
    }
}
