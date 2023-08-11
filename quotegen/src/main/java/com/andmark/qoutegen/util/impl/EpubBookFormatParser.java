package com.andmark.qoutegen.util.impl;

import com.andmark.qoutegen.domain.Book;
import com.andmark.qoutegen.domain.enums.BookFormat;
import com.andmark.qoutegen.util.BookFormatParser;
import lombok.extern.slf4j.Slf4j;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TableOfContents;
import nl.siegmann.epublib.epub.EpubReader;
import org.springframework.stereotype.Component;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
@Slf4j
public class EpubBookFormatParser implements BookFormatParser {
    @Override
    public String parse(Book book) {
        log.debug("parse epub book, title = {}", book.getTitle());
        String text = null;
        Path path = Paths.get(book.getFilePath());
        log.debug("path = {}", path);
        File file = path.toFile();
        EpubReader epubReader = new EpubReader();
        StringBuilder textBuilder = new StringBuilder();
        try {
            nl.siegmann.epublib.domain.Book epubBook = epubReader.readEpub(new FileInputStream(file));
            TableOfContents tableOfContents = epubBook.getTableOfContents();
            List<Resource> allUniqueResources = tableOfContents.getAllUniqueResources();
            for (Resource allUniqueResource : allUniqueResources) {
                byte[] data = allUniqueResource.getData();
                String element = new String(data, StandardCharsets.UTF_8);
                text = extractPlainTextFromHtml(element);
                textBuilder.append(text).append("\n");
            }
            log.debug("getting text from epub book {}", book.getTitle());
        } catch (IOException e) {
            log.warn("Error parse in file {}", path, e);
        }
        return text;
    }

    private String extractPlainTextFromHtml(String html) {
        log.debug("Jsoup parse html");
        Document doc = Jsoup.parse(html);
        return doc.text();
    }

    @Override
    public BookFormat getFormat() {
        return BookFormat.EPUB;
    }
}
