package com.andmark.qoutegen.util.impl;

import com.andmark.qoutegen.domain.Book;
import com.andmark.qoutegen.domain.enums.BookFormat;
import com.andmark.qoutegen.util.BookFormatParser;
import com.kursx.parser.fb2.Body;
import com.kursx.parser.fb2.Element;
import com.kursx.parser.fb2.FictionBook;
import com.kursx.parser.fb2.Section;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static com.andmark.qoutegen.domain.enums.BookFormat.FB2;

@Component
@Slf4j
public class Fb2BookFormatParser implements BookFormatParser {
    @Override
    public String parse(Book book) {
        log.debug("parse fb2 book, title = {}", book.getTitle());
        String text = null;
        Path path = Paths.get(book.getFilePath());
        log.debug("path = {}", path);
        File file = path.toFile();

        try {
            FictionBook fb2 = new FictionBook(new File(file.getAbsolutePath()));
            Body body = fb2.getBody();
            text = parseFB2file(body);
            log.info("get text from fb2 file with id = {}", book.getId());
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.warn("Error parse in file {}", path, e);
        }
        return text;
    }

    private String parseFB2file(Body body) {
        StringBuilder text = new StringBuilder();
        for (Section section : body.getSections()) {
            ArrayList<Element> elements = section.getElements();
            for (Element element : elements) {
                text.append(element.getText());
            }
        }
        return text.toString();
    }

    @Override
    public BookFormat getFormat() {
        return FB2;
    }
}
