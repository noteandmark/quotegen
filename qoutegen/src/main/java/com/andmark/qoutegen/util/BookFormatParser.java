package com.andmark.qoutegen.util;

import com.andmark.qoutegen.domain.Book;
import com.andmark.qoutegen.domain.enums.BookFormat;

    public interface BookFormatParser {
    String parse(Book book);

    BookFormat getFormat();
}
