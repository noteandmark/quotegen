package com.andmark.quotegen.dto;

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
    private Date usedAt;

    private String bookAuthor;
    private String bookTitle;
}
