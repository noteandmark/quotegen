package com.andmark.quotegen.util.impl;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.domain.enums.BookFormat;
import com.andmark.quotegen.util.BookFormatParser;
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
import java.util.List;

import static com.andmark.quotegen.domain.enums.BookFormat.FB2;

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
            //recursive parsing
            text = parseSection(body.getSections());
            log.info("get text from fb2 file with id = {}", book.getId());
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.warn("Error parse in file {}", path, e);
        }
        return text;
    }

    private String parseSection(List<Section> sections) {
        StringBuilder text = new StringBuilder();
        for (Section section : sections) {
            text.append(parseElements(section.getElements()));
            text.append(parseSection(section.getSections()));
        }
        return text.toString();
    }

    private String parseElements(List<Element> elements) {
        StringBuilder text = new StringBuilder();
        for (Element element : elements) {
            text.append(element.getText());
        }
        return text.toString();
    }

    @Override
    public BookFormat getFormat() {
        return FB2;
    }
}
