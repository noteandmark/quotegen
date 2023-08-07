package com.andmark.qoutegen.dto;

import com.andmark.qoutegen.domain.Book;
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

    private String content;
    private Date usedAt;
    private Book bookSource;

}
