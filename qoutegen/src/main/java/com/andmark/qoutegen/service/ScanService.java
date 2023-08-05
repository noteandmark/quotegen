package com.andmark.qoutegen.service;

import com.andmark.qoutegen.dto.BookDTO;
import com.andmark.qoutegen.model.Book;
import com.andmark.qoutegen.model.enums.BookFormat;
import com.andmark.qoutegen.repository.BooksRepository;
import com.andmark.qoutegen.util.BookFormatParser;
import com.andmark.qoutegen.util.impl.*;
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
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.andmark.qoutegen.model.enums.BookFormat.*;
import static com.andmark.qoutegen.model.enums.Status.ACTIVE;
import static com.andmark.qoutegen.model.enums.Status.DELETED;

@Service
@Transactional(readOnly = true)
public class ScanService {

    private final BooksRepository booksRepository;
    private final ModelMapper mapper;

    @Autowired
    public ScanService(BooksRepository booksRepository, ModelMapper mapper) {
        this.booksRepository = booksRepository;
        this.mapper = mapper;
    }

    @Transactional
    public List<BookDTO> scanBooks(String directoryPath) {
        File rootDirectory = new File(directoryPath);
        if (!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
        }

        //scan and find files in folder and subfolders
        List<Book> scannedBooks = getBookList(rootDirectory);

        //check if the database is up to date - if you have deleted a file from the disc
        // - put the status deleted in the database
        cleanUpDatabase(scannedBooks);

        // Save all books in a single transaction
        booksRepository.saveAll(scannedBooks);

        return scannedBooks.stream()
                .map(book -> mapper.map(book, BookDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void cleanUpDatabase(List<Book> scannedBooks) {
        List<Book> booksInDatabase = booksRepository.findByStatus(ACTIVE);
        booksInDatabase.removeAll(scannedBooks);
        if (!booksInDatabase.isEmpty()) {
            for (Book bookToDelete : booksInDatabase) {
                // mark the book as deleted
                bookToDelete.setStatus(DELETED);
                booksRepository.save(bookToDelete);
            }
        }
    }

    List<Book> getBookList(File rootDirectory) {
        List<Book> scannedBooks = new ArrayList<>();
        //recursively go through the directory and all subfolders looking for files
        scanBooksRecursive(rootDirectory, scannedBooks);
        return scannedBooks;
    }

    private void scanBooksRecursive(File directory, List<Book> scannedBooks) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    Book book = processBookFile(file);
                    if (book != null) {
                        scannedBooks.add(book);
                    }
                } else if (file.isDirectory()) {
                    scanBooksRecursive(file, scannedBooks);
                }
            }
        }
    }

    Book processBookFile(File file) {
        Book book = checkExistingBook(file);

        if (book == null) {
            BookFormat bookFormat = getBookFormat(file.getName());
            if (bookFormat != BookFormat.NOT_FOUND) {
                book = new Book();
                book.setAuthor("file.getParentFile().getName()");
                book.setFilePath(file.getPath());
                book.setTitle(removeExtension(file));
                book.setStatus(ACTIVE);
                book.setFormat(bookFormat);
            } else {
                //unknown format file
                return null;
            }
            if (book.getStatus().equals(DELETED)) book.setStatus(ACTIVE);
        }
        return book;
    }

    Book checkExistingBook(File file) {
        String filePath = file.getPath();
        Optional<Book> existingBook = booksRepository.findByFilePath(filePath);

        return existingBook.orElse(null);
    }

    BookFormat getBookFormat(String fileName) {
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

}

