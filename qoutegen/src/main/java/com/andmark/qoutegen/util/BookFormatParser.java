package com.andmark.qoutegen.util;

import com.andmark.qoutegen.models.Book;

import java.io.File;

public interface BookFormatParser {
    String parse(Book book);
}
