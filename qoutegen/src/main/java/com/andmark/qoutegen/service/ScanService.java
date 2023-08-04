package com.andmark.qoutegen.service;

import com.andmark.qoutegen.dto.BookDTO;
import com.andmark.qoutegen.model.Book;
import com.andmark.qoutegen.model.enums.BookFormat;
import com.andmark.qoutegen.repository.BooksRepository;
import com.kursx.parser.fb2.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
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

        System.out.println("rootDirectory = " + rootDirectory.getAbsolutePath());

        if (!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
        }

        List<BookDTO> scannedBooks = new ArrayList<>();
        scanBooksRecursive(rootDirectory, scannedBooks);

        // Get an array of files in the directory
//        File[] files = directory.listFiles();

//        if (files != null) {
//            for (File file : files) {
//                if (file.isFile()) {
//                    System.out.println("");
//                    // Process each file and extract book information
//                    BookDTO bookDTO = processBookFile(file);
//                    if (bookDTO != null) {
//                        scannedBooks.add(bookDTO);
//                    }
//                }
//            }
//        }

        return scannedBooks;
    }

    private void scanBooksRecursive(File directory, List<BookDTO> scannedBooks) {
        System.out.println("in scanBooksRecursive with directory = " + directory.getName());
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    System.out.println("find file : " + file.getName());
                    BookDTO bookDTO = processBookFile(file);
                    if (bookDTO != null) {
                        bookDTO.setAuthor(directory.getName());
                        scannedBooks.add(bookDTO);
                    }
                } else if (file.isDirectory()) {
                    System.out.println("file is directory: " + file.getName());
                    scanBooksRecursive(file, scannedBooks);
                }
            }
        }
    }

    private BookDTO processBookFile(File file) {
        String fileFormat;
        String fileName = file.getName();

        int lastDotIndex = fileName.lastIndexOf(".");
        fileFormat = fileName.substring(lastDotIndex + 1);
        try {
            BookFormat format = BookFormat.valueOf(fileFormat.toUpperCase());
            switch (format) {
                case FB2:
                    scanFB2Book(file);
                    break;
                case EPUB:
                    break;
                case DOC:
                    break;
                case PDF:
                    scanPDFBook(file);
                    break;
            }
        } catch (IllegalArgumentException e) {
            // Handle case when the file format is not recognized
            return null;
        }

        return null;
    }

    private BookDTO scanPDFBook(File file) {
        BookDTO bookDTO = new BookDTO();

        try {
            PDDocument document = PDDocument.load(file);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        bookDTO.setFormat(PDF);

        return bookDTO;
    }

    private BookDTO scanFB2Book(File file) {
        String filePath = file.getPath();
        Optional<Book> existingBook = booksRepository.findByFilePath(filePath);

        if (!existingBook.isEmpty()) {
            // Book with the same filePath already exists, return its DTO
            return mapper.map(existingBook, BookDTO.class);
        }

        try {
            FictionBook fb2 = new FictionBook(new File(file.getAbsolutePath()));

            Body body = fb2.getBody();

            Book book = new Book();
            book.setTitle(fb2.getTitle());
            book.setFilePath(file.getPath());
            book.setFormat(FB2);
            book.setAuthor(file.getParentFile().getName());

            Book savedBook = booksRepository.save(book);

            return mapper.map(savedBook, BookDTO.class);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return null; //TODO make exseptions
        }
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
