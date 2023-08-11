package com.andmark.quotegen.util.impl;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.domain.enums.BookFormat;
import com.andmark.quotegen.util.BookFormatParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotFoundFormatParser implements BookFormatParser {
    @Override
    public String parse(Book book) {
        log.debug("unknown book format = {}", book.getFormat());
        return "unfortunately this book format is not implemented for reading by the program";
    }

    @Override
    public BookFormat getFormat() {
        return BookFormat.NOT_FOUND;
    }
}
