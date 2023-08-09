package com.andmark.quotebot.dto;

import com.andmark.quotebot.domain.enums.BookFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class BookDTO {

    private Long id;
    private String title;
    private String author;
    private BookFormat format;
    private String filePath;

}
