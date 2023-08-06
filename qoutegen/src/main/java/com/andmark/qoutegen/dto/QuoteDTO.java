package com.andmark.qoutegen.dto;

import com.andmark.qoutegen.models.Book;
import lombok.*;

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
