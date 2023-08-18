package com.andmark.quotegen.dto;

import com.andmark.quotegen.domain.enums.QuoteStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class QuoteDTO {
    private Long id;
    private String content;
    private QuoteStatus status;
    private LocalDateTime pendingTime;
    private String imageUrl;
    private LocalDateTime usedAt;

    private String bookAuthor;
    private String bookTitle;
}
