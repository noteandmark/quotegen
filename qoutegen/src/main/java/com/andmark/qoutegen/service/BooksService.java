package com.andmark.qoutegen.service;

import com.andmark.qoutegen.models.Book;

public interface BooksService extends AbstractService<Book>{
    void clearDeletedBooks();
}
