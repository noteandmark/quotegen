package com.andmark.qoutegen.util.impl;

import com.andmark.qoutegen.domain.Book;
import com.andmark.qoutegen.domain.enums.BookFormat;
import com.andmark.qoutegen.util.BookFormatParser;
import org.springframework.stereotype.Component;

@Component
public class DocxBookFormatParser implements BookFormatParser {
    @Override
    public String parse(Book book) {
        String text;
        text = "docx_book";
        return text;
    }

    @Override
    public BookFormat getFormat() {
        return BookFormat.DOCX;
    }
}
