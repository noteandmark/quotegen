package com.andmark.quotegen.service;

import java.util.List;

public interface GoogleCustomSearchService {
    List<String> searchImagesByKeywords(String content);
}
