package com.andmark.quotegen.service;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.dto.BookDTO;
import com.andmark.quotegen.dto.StatsDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ScanService {
    List<BookDTO> scanBooks(String directoryPath);

    StatsDTO getStatistics();

    String getWebLink();
}
