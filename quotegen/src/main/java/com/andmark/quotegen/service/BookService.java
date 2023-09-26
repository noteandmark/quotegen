package com.andmark.quotegen.service;

import com.andmark.quotegen.dto.BookDTO;
import com.andmark.quotegen.dto.ExtractedLinesDTO;
import com.andmark.quotegen.dto.PageLineRequestDTO;

public interface BookService extends AbstractService<BookDTO>{
    void clearDeletedBooks();
    ExtractedLinesDTO processPageAndLineNumber(PageLineRequestDTO requestDTO);
}
