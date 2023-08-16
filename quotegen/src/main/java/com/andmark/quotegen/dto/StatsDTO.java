package com.andmark.quotegen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsDTO {
    private Long bookCount;
    private Long publishedQuotesThisYear;
    private Long pendingQuotesCount;
}
