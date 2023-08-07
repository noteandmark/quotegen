package com.andmark.qoutegen.dto;

import com.andmark.qoutegen.domain.enums.BookFormat;
import lombok.*;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class BookDTO {

    private String title;
    private String author;
    private BookFormat format;
    private String filePath;

}
