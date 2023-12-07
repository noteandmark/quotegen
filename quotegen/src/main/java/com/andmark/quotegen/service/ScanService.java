package com.andmark.quotegen.service;

import com.andmark.quotegen.dto.BookDTO;
import com.andmark.quotegen.dto.StatsDTO;

import java.util.List;

public interface ScanService {
    List<BookDTO> scanBooks(String directoryPath);

    StatsDTO getStatistics();
}
