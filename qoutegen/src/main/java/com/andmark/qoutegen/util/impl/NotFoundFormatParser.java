package com.andmark.qoutegen.util.impl;

import com.andmark.qoutegen.domain.Book;
import com.andmark.qoutegen.domain.enums.BookFormat;
import com.andmark.qoutegen.util.BookFormatParser;
import org.springframework.stereotype.Component;

@Component
public class NotFoundFormatParser implements BookFormatParser {
    @Override
    public String parse(Book book) {
        String text;
        text = "not_found_book";
        return text;
    }

    @Override
    public BookFormat getFormat() {
        return BookFormat.NOT_FOUND;
    }
}
