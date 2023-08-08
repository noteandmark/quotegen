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
import java.util.List;

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
//            text = parseFB2file(body);
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



//    private String parseFB2file(Body body) {
//        StringBuilder text = new StringBuilder();
//
//        for (Section section : body.getSections()) {
//            ArrayList<Element> elements = section.getElements();
//            for (Element element : elements) {
//                text.append(element.getText());
//            }
//
//            ArrayList<Section> section1 = section.getSections();
//            for (Section innerSection1 : section1) {
//                text.append(innerSection1);
//
//                ArrayList<Element> innerSectionElements1 = innerSection1.getElements();
//                for (Element innerSectionElement : innerSectionElements1) {
//                    text.append(innerSectionElement.getText());
//                }
//
//                ArrayList<Section> section2 = innerSection1.getSections();
//                for (Section innerSection2 : section2) {
//                    text.append(innerSection2);
//
//                    ArrayList<Element> innerSectionElements2 = innerSection2.getElements();
//                    for (Element innerSectionElement2 : innerSectionElements2) {
//                        text.append(innerSectionElement2.getText());
//                    }
//
//                    ArrayList<Section> section3 = innerSection2.getSections();
//                    for (Section innerSection3 : section3) {
//                        text.append(innerSection3);
//
//                        ArrayList<Element> innerSectionElements3 = innerSection3.getElements();
//                        for (Element innerSectionElement3 : innerSectionElements3) {
//                            text.append(innerSectionElement3.getText());
//                        }
//
//                        ArrayList<Section> section4 = innerSection3.getSections();
//                        for (Section innerSection4 : section4) {
//                            text.append(innerSection4);
//
//                            ArrayList<Element> innerSectionElements4 = innerSection4.getElements();
//                            for (Element innerSectionElement4 : innerSectionElements4) {
//                                text.append(innerSectionElement4.getText());
//                            }
//                        }
//                    }
//
//                }
//            }
//        }
//        return text.toString();
//    }

    @Override
    public BookFormat getFormat() {
        return FB2;
    }
}
