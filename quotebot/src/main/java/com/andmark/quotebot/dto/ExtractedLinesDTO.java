package com.andmark.quotebot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractedLinesDTO {
    private List<String> lines;
    private String bookAuthor;
    private String bookTitle;
}
