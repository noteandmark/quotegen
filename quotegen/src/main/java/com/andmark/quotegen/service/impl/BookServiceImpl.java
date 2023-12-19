package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.domain.enums.BookStatus;
import com.andmark.quotegen.dto.BookDTO;
import com.andmark.quotegen.dto.ExtractedLinesDTO;
import com.andmark.quotegen.dto.PageLineRequestDTO;
import com.andmark.quotegen.exception.NotFoundBookException;
import com.andmark.quotegen.repository.BooksRepository;
import com.andmark.quotegen.service.BookService;
import com.andmark.quotegen.service.QuoteService;
import com.andmark.quotegen.util.MapperConvert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class BookServiceImpl implements BookService {
    private final BooksRepository booksRepository;
    private final QuoteService quoteService;
    private final MapperConvert<Book, BookDTO> mapper;
    @Value("${quote.countLinesAtPage}")
    private int countLinesAtPage;
    @Value("${quote.maxCharactersInline}")
    private int maxCharactersInline;
    @Value("${quote.numberNextLines}")
    private int numberNextLines;

    @Autowired
    public BookServiceImpl(BooksRepository booksRepository, QuoteService quoteService, MapperConvert<Book, BookDTO> mapper) {
        this.booksRepository = booksRepository;
        this.quoteService = quoteService;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(BookDTO bookDTO) {
        log.debug("saving book");
        booksRepository.save(convertToEntity(bookDTO));
        log.info("save book {}", bookDTO);
    }

    @Override
    public BookDTO findOne(Long id) {
        log.debug("find book by id {}", id);
        Optional<Book> foundBook = booksRepository.findById(id);
        log.info("find book {}", foundBook);
        return foundBook.map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public List<BookDTO> findAll() {
        log.debug("find all books");
        List<Book> bookList = booksRepository.findAll();
        log.info("founded bookList.size = {}", bookList.size());
        return convertToDtoList(bookList);
    }

    @Override
    @Transactional
    public void update(Long id, BookDTO updatedBookDTO) {
        log.debug("update book by id {}", id);
        Book updatedBook = convertToEntity(updatedBookDTO);
        updatedBook.setId(id);
        booksRepository.save(updatedBook);
        log.info("update book {}", updatedBook);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("delete book by id {}", id);
        booksRepository.deleteById(id);
        log.info("delete book with id {} perform", id);
    }

    @Transactional
    public void clearDeletedBooks() {
        log.debug("in clearDeletedBooks");
        List<Book> deletedBooks = booksRepository.findByBookStatus(BookStatus.DELETED);
        log.debug("books that will be deleted: " + deletedBooks);
        booksRepository.deleteAll(deletedBooks);
        log.info("service clearDeletedBooks perform");
    }

    public ExtractedLinesDTO processPageAndLineNumber(PageLineRequestDTO requestDTO) {
        int pageNumber = requestDTO.getPageNumber();
        int lineNumber = requestDTO.getLineNumber();
        log.debug("bookService make divination with pageNumber = {} , lineNumber = {}", pageNumber, lineNumber);

        List<Book> activeBooks = getActiveBooks();
        Book selectedBook = null;

        String bookContent = "";
        int limitCounter = 0;
        while (bookContent.isEmpty()) {
            selectedBook = selectRandomBook(activeBooks);
            log.debug("selected book has id = {}", selectedBook.getId());
            bookContent = getBookContent(selectedBook);
            log.debug("bookContent length = {}", bookContent.length());
            limitCounter++;
            if (limitCounter > 10) {
                log.error("10 times failed to found book with not null bookContent!");
                bookContent = "К сожалению, сегодня не удалось ничего найти. Попробуйте завтра.";
            }
        }

        List<String> lines = breakContentIntoLines(bookContent);

        int totalPages = calculateTotalPages(lines.size());
        pageNumber = adjustPageNumber(pageNumber, totalPages);
        int startIndex = calculateStartIndex(pageNumber);
        lineNumber = adjustLineNumber(lineNumber);
        int userLineIndex = calculateUserLineIndex(startIndex, lineNumber);

        List<String> extractedLines = extractRequestedLines(lines, userLineIndex);
        log.info("return extractedLines: {}", extractedLines);

        ExtractedLinesDTO dto = createDTO(extractedLines, selectedBook);
        return dto;
    }

    List<Book> getActiveBooks() {
        List<Book> activeBooks = booksRepository.findByBookStatus(BookStatus.ACTIVE);
        if (activeBooks.isEmpty()) {
            log.warn("No active books available.");
            throw new NotFoundBookException("No active books available.");
        }
        log.debug("find activeBooks in size: {}", activeBooks.size());
        return activeBooks;
    }

    Book selectRandomBook(List<Book> activeBooks) {
        Random random = new Random();
        Book selectedBook = activeBooks.get(random.nextInt(activeBooks.size()));
        log.debug("select book with id = {}", selectedBook.getId());
        return selectedBook;
    }

    String getBookContent(Book selectedBook) {
        String bookContent = quoteService.getBookText(selectedBook);
        log.debug("length of bookContent = {}", bookContent.length());
        return bookContent;
    }

    List<String> breakContentIntoLines(String bookContent) {
        List<String> lines = new ArrayList<>();
        int startPos = 0;
        while (startPos < bookContent.length()) {
            int endPos = calculateEndPosition(bookContent, startPos);
            lines.add(bookContent.substring(startPos, endPos).trim());
            startPos = endPos;
        }
        log.debug("count of book's lines = {}", lines.size());
        return lines;
    }

    private int calculateEndPosition(String bookContent, int startPos) {
        int remainingLength = bookContent.length() - startPos;
        int endPos = Math.min(startPos + maxCharactersInline, bookContent.length());

        // Check if the remaining length is less than the specified limit
        if (remainingLength < maxCharactersInline) {
            return bookContent.length();
        }

        while (endPos > startPos && !Character.isWhitespace(bookContent.charAt(endPos - 1))) {
            endPos--;
        }

        if (endPos == startPos) {
            while (endPos < bookContent.length() && !Character.isWhitespace(bookContent.charAt(endPos))) {
                endPos++;
            }
        }
        return endPos;
    }

    int calculateTotalPages(int totalLines) {
        return (int) Math.ceil((double) totalLines / countLinesAtPage);
    }

    int adjustPageNumber(int pageNumber, int totalPages) {
        return (pageNumber - 1) % totalPages + 1;
    }

    int calculateStartIndex(int pageNumber) {
        return (pageNumber - 1) * countLinesAtPage;
    }

    int adjustLineNumber(int lineNumber) {
        return (lineNumber - 1) % countLinesAtPage + 1;
    }

    int calculateUserLineIndex(int startIndex, int lineNumber) {
        return startIndex + lineNumber - 1;
    }

    List<String> extractRequestedLines(List<String> lines, int userLineIndex) {
        List<String> extractedLines = new ArrayList<>();
        for (int i = userLineIndex; i < lines.size() && i < userLineIndex + numberNextLines; i++) {
            extractedLines.add(lines.get(i));
        }
        return extractedLines;
    }

    ExtractedLinesDTO createDTO(List<String> extractedLines, Book selectedBook) {
        ExtractedLinesDTO dto = new ExtractedLinesDTO();
        dto.setLines(extractedLines);
        dto.setBookAuthor(selectedBook.getAuthor());
        dto.setBookTitle(selectedBook.getTitle());
        return dto;
    }

    public BookDTO convertToDTO(Book book) {
        return mapper.convertToDTO(book, BookDTO.class);
    }

    public Book convertToEntity(BookDTO bookDTO) {
        return mapper.convertToEntity(bookDTO, Book.class);
    }

    List<BookDTO> convertToDtoList(List<Book> books) {
        return books.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

}
