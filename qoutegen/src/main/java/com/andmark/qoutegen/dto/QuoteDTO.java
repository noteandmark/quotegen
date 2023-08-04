package com.andmark.qoutegen.dto;

import com.andmark.qoutegen.model.Book;
import lombok.Data;

import java.util.Date;

@Data
public class QuoteDTO {

    private Long id;
    private String content;
    private Date usedAt;
    private Book bookSource;

}
