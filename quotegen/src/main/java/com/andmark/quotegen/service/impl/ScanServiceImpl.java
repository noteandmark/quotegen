package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.domain.enums.BookFormat;
import com.andmark.quotegen.domain.enums.QuoteStatus;
import com.andmark.quotegen.dto.BookDTO;
import com.andmark.quotegen.dto.StatsDTO;
import com.andmark.quotegen.exception.InvalidWebLinkException;
import com.andmark.quotegen.exception.ServiceException;
import com.andmark.quotegen.repository.BooksRepository;
import com.andmark.quotegen.repository.QuotesRepository;
import com.andmark.quotegen.service.ScanService;
import com.andmark.quotegen.util.NgrokUrlHolder;
import com.andmark.quotegen.util.NgrokWebServerEventListener;
import com.andmark.quotegen.util.UrlValidator;
import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.andmark.quotegen.domain.enums.BookStatus.ACTIVE;
import static com.andmark.quotegen.domain.enums.BookStatus.DELETED;

@Service
@Transactional(readOnly = true)
@Slf4j
public class ScanServiceImpl implements ScanService {
    private final NgrokWebServerEventListener ngrokWebServerEventListener;
    private final BooksRepository booksRepository;
    private final QuotesRepository quotesRepository;
    private final ModelMapper mapper;

    @Autowired
    public ScanServiceImpl(NgrokWebServerEventListener ngrokWebServerEventListener, BooksRepository booksRepository, QuotesRepository quotesRepository, ModelMapper mapper) {
        this.ngrokWebServerEventListener = ngrokWebServerEventListener;
        this.booksRepository = booksRepository;
        this.quotesRepository = quotesRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public List<BookDTO> scanBooks(String directoryPath) {
        log.debug("in scanBooks with directoryPath = " + directoryPath);
        File rootDirectory = new File(directoryPath);
        if (!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            log.error("Invalid directory path: " + directoryPath);
            throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
        }

        //scan and find files in folder and subfolders
        List<Book> scannedBooks;
        try {
            scannedBooks = getBookList(rootDirectory);
        } catch (IllegalArgumentException e) {
            log.error("Error while scanning books in directory: {}", directoryPath, e);
            throw new ServiceException("Error while scanning books.", e);
        }

        //check if the database is up to date - if you have deleted a file from the disc
        // - put the status deleted in the database
        cleanUpDatabase(scannedBooks);

        // Save all books in a single transaction
        booksRepository.saveAll(scannedBooks);

        log.info("Scanned and saved {} books", scannedBooks.size());

        return scannedBooks.stream()
                .map(book -> mapper.map(book, BookDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void cleanUpDatabase(List<Book> scannedBooks) {
        log.debug("check if needed to set status deleted ");
        List<Book> booksInDatabase = booksRepository.findByBookStatus(ACTIVE);
        booksInDatabase.removeAll(scannedBooks);
        if (!booksInDatabase.isEmpty()) {
            log.info("cleaning books with status deleted: {}", booksInDatabase);
            for (Book bookToDelete : booksInDatabase) {
                // mark the book as deleted
                bookToDelete.setBookStatus(DELETED);
                log.debug("setStatus DELETED to {}", bookToDelete.getFilePath());
                booksRepository.save(bookToDelete);
                log.info("booksRepository.save(bookToDelete) perform");
            }
        }
    }

    List<Book> getBookList(File rootDirectory) {
        log.debug("Getting list of books from directory: {}", rootDirectory);
        List<Book> scannedBooks = new ArrayList<>();
        //recursively go through the directory and all subfolders looking for files
        scanBooksRecursive(rootDirectory, scannedBooks);
        return scannedBooks;
    }

    private void scanBooksRecursive(File directory, List<Book> scannedBooks) {
        log.debug("Scanning directory: {}", directory);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                Book book = null;
                if (file.isFile()) {
                    try {
                        book = processBookFile(file);
                    } catch (Exception e) {
                        log.error("Error while processing book file: {}", file, e);
                    }

                    if (book != null) {
                        scannedBooks.add(book);
                    }
                } else if (file.isDirectory()) {
                    scanBooksRecursive(file, scannedBooks);
                }
            }
        }
    }

    public Book processBookFile(File file) {
        log.debug("Processing book file: {}", file);
        Book book = checkExistingBook(file);

        if (book == null) {
            BookFormat bookFormat = getBookFormat(file.getName());
            if (bookFormat != BookFormat.NOT_FOUND) {
                book = new Book();
                book.setAuthor(file.getParentFile().getName());
                book.setFilePath(file.getPath());
                book.setTitle(removeExtension(file));
                book.setBookStatus(ACTIVE);
                book.setFormat(bookFormat);
            } else {
                log.warn("unknown format file for application");
                return null;
            }
        } else {
            if (book.getBookStatus().equals(DELETED)) book.setBookStatus(ACTIVE);
        }
        return book;
    }

    public Book checkExistingBook(File file) {
        log.debug("Checking existing book for file: {}", file);
        String filePath = file.getPath();
        Optional<Book> existingBook = booksRepository.findByFilePath(filePath);

        return existingBook.orElse(null);
    }

    public BookFormat getBookFormat(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        String fileFormat = fileName.substring(lastDotIndex + 1).toUpperCase();
        return BookFormat.fromString(fileFormat);
    }

    public String removeExtension(File file) {
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }

    public StatsDTO getStatistics() {
        log.debug("scan service: getStatistics");
        LocalDate currentDate = LocalDate.now();
        LocalDate startOfYear = currentDate.with(TemporalAdjusters.firstDayOfYear());
        log.debug("startOfYear = {}", startOfYear);

        Long bookCount = booksRepository.count();
        log.debug("bookCount= " + bookCount);

        LocalDateTime startOfYearDateTime = startOfYear.atStartOfDay();
        Long publishedQuotesThisYear = quotesRepository.countByStatusAndUsedAtAfter(QuoteStatus.PUBLISHED, startOfYearDateTime);
        log.debug("publishedQuotesThisYear = " + publishedQuotesThisYear);

        Long pendingQuotesCount = quotesRepository.countByStatus(QuoteStatus.PENDING);
        log.debug("pendingQuotesCount = " + pendingQuotesCount);

        log.info("getting stats: {} , {} , {}", bookCount, publishedQuotesThisYear, pendingQuotesCount);
        return new StatsDTO(bookCount, publishedQuotesThisYear, pendingQuotesCount);
    }

    @Override
    public String getWebLink() {
        log.debug("scan service: get web link");
        String publicUrl = ngrokWebServerEventListener.getWebLink();
        log.debug("web link: {}", publicUrl);
        if (UrlValidator.isValidUrl(publicUrl)) {
            log.debug("web link valid, return");
            return publicUrl;
        } else {
            log.error("web link does not valid");
            throw new InvalidWebLinkException("Invalid web link: " + publicUrl);
        }
    }

}

