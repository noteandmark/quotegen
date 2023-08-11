package com.andmark.quotegen.util.impl;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.domain.enums.BookFormat;
import com.andmark.quotegen.util.BookFormatParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
public class PdfBookFormatParser implements BookFormatParser {
    @Override
    public String parse(Book book) {
        log.debug("parse pdf book, title = {}", book.getTitle());
        String text = null;
        Path path = Paths.get(book.getFilePath());
        log.debug("path = {}", path);
        File file = path.toFile();

        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(document);
            log.debug("get text from pdf book with id = {}", book.getId());
        } catch (IOException e) {
            log.warn("Error parse in file {}", path, e);
        }
        return text;
    }

    @Override
    public BookFormat getFormat() {
        return BookFormat.PDF;
    }
}
