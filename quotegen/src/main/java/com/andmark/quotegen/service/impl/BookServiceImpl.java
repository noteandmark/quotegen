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
import com.andmark.quotegen.util.impl.MapperConvert;
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
    private final int countLinesAtPage;
    private final int maxCharactersInline;
    private final int numberNextLines;

    @Autowired
    public BookServiceImpl(BooksRepository booksRepository, QuoteService quoteService, MapperConvert<Book, BookDTO> mapper,
                           @Value("${quote.countLinesAtPage}") int countLinesAtPage,
                           @Value("${quote.maxCharactersInline}") int maxCharactersInline,
                           @Value("${quote.numberNextLines}") int numberNextLines) {
        this.booksRepository = booksRepository;
        this.quoteService = quoteService;
        this.mapper = mapper;
        this.countLinesAtPage = countLinesAtPage;
        this.maxCharactersInline = maxCharactersInline;
        this.numberNextLines = numberNextLines;
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
        log.info("founded bookList = {}", bookList);
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
        Book selectedBook = selectRandomBook(activeBooks);
        String bookContent = getBookContent(selectedBook);
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

    private List<Book> getActiveBooks() {
        List<Book> activeBooks = booksRepository.findByBookStatus(BookStatus.ACTIVE);
        if (activeBooks.isEmpty()) {
            log.warn("No active books available.");
            throw new NotFoundBookException("No active books available.");
        }
        log.debug("find activeBooks in size: {}", activeBooks.size());
        return activeBooks;
    }

    private Book selectRandomBook(List<Book> activeBooks) {
        Random random = new Random();
        Book selectedBook = activeBooks.get(random.nextInt(activeBooks.size()));
        log.debug("select book with id = {}", selectedBook.getId());
        return selectedBook;
    }

    private String getBookContent(Book selectedBook) {
        String bookContent = quoteService.getBookText(selectedBook);
        log.debug("length of bookContent = {}", bookContent.length());
        return bookContent;
    }

    private List<String> breakContentIntoLines(String bookContent) {
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
        int endPos = Math.min(startPos + maxCharactersInline, bookContent.length());
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

    private int calculateTotalPages(int totalLines) {
        return (int) Math.ceil((double) totalLines / countLinesAtPage);
    }

    private int adjustPageNumber(int pageNumber, int totalPages) {
        return (pageNumber - 1) % totalPages + 1;
    }

    private int calculateStartIndex(int pageNumber) {
        return (pageNumber - 1) * countLinesAtPage;
    }

    private int adjustLineNumber(int lineNumber) {
        return (lineNumber - 1) % countLinesAtPage + 1;
    }

    private int calculateUserLineIndex(int startIndex, int lineNumber) {
        return startIndex + lineNumber - 1;
    }

    private List<String> extractRequestedLines(List<String> lines, int userLineIndex) {
        List<String> extractedLines = new ArrayList<>();
        for (int i = userLineIndex; i < lines.size() && i < userLineIndex + numberNextLines; i++) {
            extractedLines.add(lines.get(i));
        }
        return extractedLines;
    }

    private ExtractedLinesDTO createDTO(List<String> extractedLines, Book selectedBook) {
        ExtractedLinesDTO dto = new ExtractedLinesDTO();
        dto.setLines(extractedLines);
        dto.setBookAuthor(selectedBook.getAuthor());
        dto.setBookTitle(selectedBook.getTitle());
        return dto;
    }


//    @Override
//    public ExtractedLinesDTO processPageAndLineNumber(PageLineRequestDTO requestDTO) {
//        // Extract page and line numbers from the requestDTO
//        int pageNumber = requestDTO.getPageNumber();
//        int lineNumber = requestDTO.getLineNumber();
//        log.debug("bookService make divination with pageNumber = {} , lineNumber = {}", pageNumber);
//        // Fetch all active books from the database
//        List<Book> activeBooks = booksRepository.findByBookStatus(BookStatus.ACTIVE);
//        log.debug("find activeBooks in size: {}", activeBooks.size());
//        if (activeBooks.isEmpty()) {
//            log.warn("No active books available.");
//            throw new NotFoundBookException("No active books available.");
//        }
//        // Randomly select one book from the list of active books
//        Random random = new Random();
//        Book selectedBook = activeBooks.get(random.nextInt(activeBooks.size()));
//        log.debug("select book with id = {}", selectedBook.getId());
//        // Parse the selected book's text to get the content
//        String bookContent = quoteService.getBookText(selectedBook);
//        log.debug("length of bookContent = {}", bookContent.length());
//
//        // Break the content into lines
//        List<String> lines = new ArrayList<>();
//        int startPos = 0;
//        while (startPos < bookContent.length()) {
//            int endPos = Math.min(startPos + maxCharactersInline, bookContent.length());
//
//            // Move endPos to the left until a whitespace character is found
//            while (endPos > startPos && !Character.isWhitespace(bookContent.charAt(endPos - 1))) {
//                endPos--;
//            }
//            // Check if the endPos reached startPos
//            if (endPos == startPos) {
//                // Move endPos to the right until a whitespace character is found
//                while (endPos < bookContent.length() && !Character.isWhitespace(bookContent.charAt(endPos))) {
//                    endPos++;
//                }
//            }
//            lines.add(bookContent.substring(startPos, endPos).trim());
//            startPos = endPos;
//        }
//        log.debug("count of book's lines = {}", lines.size());
//
//        // Calculate the total number of pages based on lines per page
//        int totalPages = (int) Math.ceil((double) lines.size() / countLinesAtPage);
//        log.debug("totalPages in book = {}", totalPages);
//        // Adjust page number if it exceeds the total number of pages
//        pageNumber = (pageNumber - 1) % totalPages + 1;
//        log.debug("pageNumber = {}", pageNumber);
//
//        // Calculate the starting index of the requested page
//        int startIndex = (pageNumber - 1) * countLinesAtPage;
//        log.debug("startIndex = {}", startIndex);
//
//        // Adjust line number if it exceeds the total number of lines in page
//        lineNumber = (lineNumber - 1) % countLinesAtPage + 1;
//        // Calculate the index of the user-defined line within the requested page
//        int userLineIndex = startIndex + lineNumber - 1;
//        log.debug("userLineIndex = {}", userLineIndex);
//
//        // Extract the requested lines and the next n lines (numberNextLines)
//        List<String> extractedLines = new ArrayList<>();
//        log.debug("lines.size() = {}", lines.size());
//        log.debug("userLineIndex + numberNextLines - 1 = {}", userLineIndex + numberNextLines - 1);
//        for (int i = userLineIndex; i < lines.size() && i < userLineIndex + numberNextLines; i++) {
//            extractedLines.add(lines.get(i));
//        }
//        log.info("return extractedLines: {}", extractedLines);
//        // Create and return the DTO
//        ExtractedLinesDTO dto = new ExtractedLinesDTO();
//        dto.setLines(extractedLines);
//        dto.setBookAuthor(selectedBook.getAuthor());
//        dto.setBookTitle(selectedBook.getTitle());
//        return dto;
//    }

    public BookDTO convertToDTO(Book book) {
        return mapper.convertToDTO(book, BookDTO.class);
    }

    public Book convertToEntity(BookDTO bookDTO) {
        return mapper.convertToEntity(bookDTO, Book.class);
    }

    private List<BookDTO> convertToDtoList(List<Book> books) {
        return books.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
