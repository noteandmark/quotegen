package com.andmark.quotegen.service;

import com.andmark.quotegen.dto.BookDTO;

public interface BooksService extends AbstractService<BookDTO>{
    void clearDeletedBooks();
}
