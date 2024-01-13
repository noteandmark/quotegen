package com.andmark.quotegen.service;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.dto.BookDTO;
import com.andmark.quotegen.dto.ExtractedLinesDTO;
import com.andmark.quotegen.dto.PageLineRequestDTO;

import java.util.List;

public interface BookService extends AbstractService<BookDTO>{
    void clearDeletedBooks();
    ExtractedLinesDTO processPageAndLineNumber(PageLineRequestDTO requestDTO);

    List<Book> getDeletedBooks();
}
