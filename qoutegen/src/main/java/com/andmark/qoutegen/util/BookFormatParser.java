package com.andmark.qoutegen.util;

import com.andmark.qoutegen.model.Book;

import java.io.File;

public interface BookFormatParser {
    Book parse(File file);
}
