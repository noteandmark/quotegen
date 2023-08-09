package com.andmark.qoutegen.util.impl;

import com.andmark.qoutegen.domain.Book;
import com.andmark.qoutegen.domain.enums.BookFormat;
import com.andmark.qoutegen.util.BookFormatParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.extractor.POITextExtractor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Component
@Slf4j
public class DocBookFormatParser implements BookFormatParser {
    @Override
    public String parse(Book book) {
        log.debug("parse doc book, title = {}", book.getTitle());
        String text = null;
        Path path = Paths.get(book.getFilePath());
        log.debug("path = {}", path);

        try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ);
             POITextExtractor extractor = ExtractorFactory.createExtractor(is)) {
            log.debug("getting text from doc book {}", book.getTitle());
            text = extractor.getText();
        } catch (Exception e) {
            log.warn("Error parse in file {}", path, e);
        }
        return text;
    }

    @Override
    public BookFormat getFormat() {
        return BookFormat.DOC;
    }
}
