package com.andmark.qoutegen.util.impl;

import com.andmark.qoutegen.models.Book;
import com.andmark.qoutegen.util.BookFormatParser;

import java.io.File;

public class NotFoundFormatParser implements BookFormatParser {
    @Override
    public String parse(Book book) {
        return null;
    }
}
