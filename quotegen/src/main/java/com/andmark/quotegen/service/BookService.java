package com.andmark.quotegen.service;

import com.andmark.quotegen.dto.BookDTO;

public interface BookService extends AbstractService<BookDTO>{
    void clearDeletedBooks();
}
