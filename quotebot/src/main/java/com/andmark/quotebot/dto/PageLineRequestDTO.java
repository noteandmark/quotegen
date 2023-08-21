package com.andmark.quotebot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageLineRequestDTO {
    private int pageNumber;
    private int lineNumber;
}
