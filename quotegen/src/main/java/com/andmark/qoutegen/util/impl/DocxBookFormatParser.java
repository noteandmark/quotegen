package com.andmark.qoutegen.util.impl;

import com.andmark.qoutegen.domain.Book;
import com.andmark.qoutegen.domain.enums.BookFormat;
import com.andmark.qoutegen.util.BookFormatParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
public class DocxBookFormatParser implements BookFormatParser {
    @Override
    public String parse(Book book) {
        log.debug("parse docx book, title = {}", book.getTitle());
        String text = null;
        Path path = Paths.get(book.getFilePath());
        log.debug("path = {}", path);
        File file = path.toFile();

        try (FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath())) {
            XWPFDocument docxFile = new XWPFDocument(OPCPackage.open(fileInputStream));
            XWPFWordExtractor extractor = new XWPFWordExtractor(docxFile);
            log.debug("getting text from docx book {}", book.getTitle());
            text = extractor.getText();
        } catch (Exception e) {
            log.warn("Error parse in file {}", path, e);
        }
        return text;
    }

    @Override
    public BookFormat getFormat() {
        return BookFormat.DOCX;
    }
}
