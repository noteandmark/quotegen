package com.andmark.quotebot.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsDTO {
    private Long bookCount;
    private Long publishedQuotesThisYear;
    private Long pendingQuotesCount;
}
