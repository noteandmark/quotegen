package com.andmark.qoutegen.service;

import com.andmark.qoutegen.domain.Book;

public interface BooksService extends AbstractService<Book>{
    void clearDeletedBooks();
}
