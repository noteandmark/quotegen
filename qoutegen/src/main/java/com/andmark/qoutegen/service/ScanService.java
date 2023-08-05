package com.andmark.qoutegen.service;

import com.andmark.qoutegen.dto.BookDTO;
import com.andmark.qoutegen.model.Book;
import com.andmark.qoutegen.model.enums.BookFormat;
import com.andmark.qoutegen.repository.BooksRepository;
import com.kursx.parser.fb2.*;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TableOfContents;
import nl.siegmann.epublib.epub.EpubReader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.andmark.qoutegen.model.enums.BookFormat.*;

@Service
public class ScanService {

    private final BooksRepository booksRepository;
    private final ModelMapper mapper;

    @Autowired
    public ScanService(BooksRepository booksRepository, ModelMapper mapper) {
        this.booksRepository = booksRepository;
        this.mapper = mapper;
    }

    public List<BookDTO> scanBooks(String directoryPath) {
        File rootDirectory = new File(directoryPath);

        if (!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
        }

        List<BookDTO> scannedBooks = getBookDTOList(rootDirectory);

        return scannedBooks;
    }

    public List<BookDTO> getBookDTOList(File rootDirectory) {
        List<BookDTO> scannedBooks = new ArrayList<>();
        scanBooksRecursive(rootDirectory, scannedBooks);
        return scannedBooks;
    }

    protected void scanBooksRecursive(File directory, List<BookDTO> scannedBooks) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    BookDTO bookDTO = processBookFile(file);
                    if (bookDTO != null) {
                        bookDTO.setAuthor(directory.getName());
                        scannedBooks.add(bookDTO);
                    }
                } else if (file.isDirectory()) {
                    scanBooksRecursive(file, scannedBooks);
                }
            }
        }
    }

    private BookDTO processBookFile(File file) {
        Book book = checkExistingBook(file);

        if (book == null) {

            BookFormat bookFormat = getBookFormat(file.getName());
            book = new Book();
            book.setAuthor(file.getParentFile().getName());
            book.setFilePath(file.getPath());
            book.setTitle(removeExtension(file));

            switch (bookFormat) {
                case FB2 -> book = scanFB2Book(book, file);
                case EPUB -> book = scanEPUBBook(book, file);
                case DOC -> book = scanDOCBook(book, file);
                case DOCX -> book = scanDOCXBook(book, file);
                case PDF -> book = scanPDFBook(book, file);
                case NOT_FOUND -> {
                    return null;
                }
            }
        }
        return mapper.map(book, BookDTO.class);
    }

    private Book checkExistingBook(File file) {
        String filePath = file.getPath();
        Optional<Book> existingBook = booksRepository.findByFilePath(filePath);

        return existingBook.orElse(null);
    }

    private Book scanEPUBBook(Book book, File file) {
        EpubReader epubReader = new EpubReader();
        StringBuilder textBuilder = new StringBuilder();
        try {
            nl.siegmann.epublib.domain.Book epubBook = epubReader.readEpub(new FileInputStream(file));
            TableOfContents tableOfContents = epubBook.getTableOfContents();
            List<Resource> allUniqueResources = tableOfContents.getAllUniqueResources();
            for (Resource allUniqueResource : allUniqueResources) {
                byte[] data = allUniqueResource.getData();
                String element = new String(data, StandardCharsets.UTF_8);
                String plainText = extractPlainTextFromHtml(element);
                textBuilder.append(plainText).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        book.setFormat(EPUB);
//        book.setFormat(BookFormat.valueOf(setExtension(file)));

        return booksRepository.save(book);
    }

    private String extractPlainTextFromHtml(String html) {
        Document doc = Jsoup.parse(html);
        return doc.text();
    }

    private Book scanDOCBook(Book book, File file) {

        POITextExtractor extractor = null;
        try {
            extractor = ExtractorFactory.createExtractor(file);
//            System.out.println(extractor.getText());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (OpenXML4JException e) {
            throw new RuntimeException(e);
        } catch (XmlException e) {
            throw new RuntimeException(e);
        }

        book.setFormat(DOC);

        return booksRepository.save(book);
    }

    private Book scanDOCXBook(Book book, File file) {

        try (FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath())) {
            XWPFDocument docxFile = new XWPFDocument(OPCPackage.open(fileInputStream));
            XWPFWordExtractor extractor = new XWPFWordExtractor(docxFile);
//            System.out.println(extractor.getText());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        book.setFormat(DOCX);

        return booksRepository.save(book);
    }

    private Book scanPDFBook(Book book, File file) {

        try {
            PDDocument document = PDDocument.load(file);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
        } catch (IOException e) {
            throw new RuntimeException(e);  //TODO make exseptions
        }

        book.setFormat(PDF);

        Book savedBook = booksRepository.save(book);

        return savedBook;
    }

    private Book scanFB2Book(Book newBook, File file) {
        String filePath = file.getPath();
        Optional<Book> existingBook = booksRepository.findByFilePath(filePath);

        if (!existingBook.isEmpty()) {
            // Book with the same filePath already exists, return its DTO
            return existingBook.get();
        }

        try {
            FictionBook fb2 = new FictionBook(new File(file.getAbsolutePath()));

            Body body = fb2.getBody();

            Book book = new Book();

            book.setAuthor(file.getParentFile().getName());
            book.setFilePath(file.getPath());
            book.setTitle(file.getName());

            book.setFormat(FB2);

            Book savedBook = booksRepository.save(book);

            return savedBook;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return null; //TODO make exseptions
        }
    }

    private BookFormat getBookFormat(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        String fileFormat = fileName.substring(lastDotIndex + 1).toUpperCase();
        return BookFormat.fromString(fileFormat);
    }

    private String removeExtension(File file) {
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }

    private String setExtension(File file) {
        String fileName = file.getName();
        int lastDotIndex = file.getName().lastIndexOf(".");
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1).toUpperCase();
        }
        return fileName;
    }

}


// реализация парсинга fb2 файла, будет использовано в дальнейшем
//            for (Section section : body.getSections()) {
//                System.out.println("section :");
//                System.out.println(section);
//
//                ArrayList<Element> elements = section.getElements();
//                for (Element element : elements) {
//                    System.out.println("element:");
//                    System.out.println(element.getText());
//                }
//
//                ArrayList<Section> section1 = section.getSections();
//                for (Section innerSection1 : section1) {
////                    System.out.println(innerSection1);
//
//                    ArrayList<Element> innerSectionElements1 = innerSection1.getElements();
//                    for (Element innerSectionElement : innerSectionElements1) {
////                        System.out.println(innerSectionElement.getText());
//                    }
//
//                    ArrayList<Section> section2 = innerSection1.getSections();
//                    for (Section innerSection2 : section2) {
////                        System.out.println(innerSection2);
//
//                        ArrayList<Element> innerSectionElements2 = innerSection2.getElements();
//                        for (Element innerSectionElement2 : innerSectionElements2) {
////                            System.out.println(innerSectionElement2.getText());
//                        }
//                    }
//                }
//            }
