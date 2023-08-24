package com.andmark.quotegen.util;

import com.andmark.quotegen.domain.enums.BookFormat;
import com.andmark.quotegen.util.impl.*;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookFormatParserFactoryTest {
    @InjectMocks
    private BookFormatParserFactory bookFormatParserFactory;

    @Test
    public void testCreateParser_PDF() {
        BookFormatParser result = bookFormatParserFactory.createParser(BookFormat.PDF);
        assertTrue(result instanceof PdfBookFormatParser);
    }

    @Test
    public void testCreateParser_DOC() {
        BookFormatParser result = bookFormatParserFactory.createParser(BookFormat.DOC);
        assertTrue(result instanceof DocBookFormatParser);
    }

    @Test
    public void testCreateParser_DOCX() {
        BookFormatParser result = bookFormatParserFactory.createParser(BookFormat.DOCX);
        assertTrue(result instanceof DocxBookFormatParser);
    }

    @Test
    public void testCreateParser_EPUB() {
        BookFormatParser result = bookFormatParserFactory.createParser(BookFormat.EPUB);
        assertTrue(result instanceof EpubBookFormatParser);
    }

    @Test
    public void testCreateParser_NOT_FOUND() {
        BookFormatParser result = bookFormatParserFactory.createParser(BookFormat.NOT_FOUND);
        assertTrue(result instanceof NotFoundFormatParser);
    }

    @Test
    public void testCreateParser_FB2() {
        BookFormatParser result = bookFormatParserFactory.createParser(BookFormat.FB2);
        assertTrue(result instanceof Fb2BookFormatParser);
    }
}