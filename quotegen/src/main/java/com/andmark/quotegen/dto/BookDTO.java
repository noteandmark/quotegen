package com.andmark.quotegen.dto;

import com.andmark.quotegen.domain.enums.BookFormat;
import com.andmark.quotegen.domain.enums.BookStatus;
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
    private BookStatus bookStatus;

}
