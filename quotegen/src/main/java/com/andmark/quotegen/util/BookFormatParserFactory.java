package com.andmark.quotegen.util;

import com.andmark.quotegen.domain.enums.BookFormat;
import com.andmark.quotegen.util.impl.*;
import org.springframework.stereotype.Component;

@Component
public class BookFormatParserFactory {
    public BookFormatParser createParser(BookFormat format) {
        switch (format) {
            case PDF:
                return new PdfBookFormatParser();
            case DOC:
                return new DocBookFormatParser();
            case DOCX:
                return new DocxBookFormatParser();
            case EPUB:
                return new EpubBookFormatParser();
            case NOT_FOUND:
                return new NotFoundFormatParser();
            case FB2:
                return new Fb2BookFormatParser();
            default:
                throw new IllegalArgumentException("Unsupported book format: " + format);
        }
    }
}
