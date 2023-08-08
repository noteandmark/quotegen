package com.andmark.qoutegen.service;

import com.andmark.qoutegen.dto.BookDTO;

public interface BooksService extends AbstractService<BookDTO>{
    void clearDeletedBooks();
}
