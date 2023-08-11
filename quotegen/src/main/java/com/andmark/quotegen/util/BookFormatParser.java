package com.andmark.quotegen.util;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.domain.enums.BookFormat;

public interface BookFormatParser {
    String parse(Book book);

    BookFormat getFormat();
}
