package com.andmark.quotegen.dto;

import com.andmark.quotegen.domain.enums.BookFormat;
import com.andmark.quotegen.domain.enums.BookStatus;
import lombok.*;

@Builder
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

    public BookDTO() {
    }

    public BookDTO(Long id, String title, String author, BookFormat format, String filePath, BookStatus bookStatus) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.format = format;
        this.filePath = filePath;
        this.bookStatus = bookStatus;
    }
}
