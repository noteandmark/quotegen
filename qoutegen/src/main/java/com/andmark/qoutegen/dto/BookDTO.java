package com.andmark.qoutegen.dto;

import com.andmark.qoutegen.model.enums.BookFormat;
import lombok.Data;

@Data
public class BookDTO {

    private Long id;
    private String title;
    private String author;
    private BookFormat format;
    private String filePath;

}
