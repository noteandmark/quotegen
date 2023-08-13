package com.andmark.quotebot.dto;

import com.andmark.quotebot.domain.enums.QuoteStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class QuoteDTO {
    private Long id;
    private String content;
    private QuoteStatus status;
    private Date pendingTime;
    private String imageUrl;
    private Date usedAt;

    private String bookAuthor;
    private String bookTitle;
}
