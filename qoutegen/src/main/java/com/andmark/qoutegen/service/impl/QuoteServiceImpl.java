package com.andmark.qoutegen.service.impl;

import com.andmark.qoutegen.exceptions.ServiceException;
import com.andmark.qoutegen.models.Book;
import com.andmark.qoutegen.models.Quote;
import com.andmark.qoutegen.models.enums.Status;
import com.andmark.qoutegen.repository.BooksRepository;
import com.andmark.qoutegen.repository.QuotesRepository;
import com.andmark.qoutegen.service.QuoteService;
import com.andmark.qoutegen.util.BookFormatParser;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
@Slf4j
public class QuoteServiceImpl implements QuoteService {
    @Value("${quote.cache.size}")
    private int cacheSize;

    private final QuotesRepository quotesRepository;
    private final BooksRepository booksRepository;
    private final BookFormatParser bookFormatParser;
    private final ModelMapper mapper;
    private final Queue<Quote> quoteCache;

    @Autowired
    public QuoteServiceImpl(QuotesRepository quotesRepository, ModelMapper mapper, Queue<Quote> quoteCache, BooksRepository booksRepository, BookFormatParser bookFormatParser) {
        this.quotesRepository = quotesRepository;
        this.mapper = mapper;
        this.booksRepository = booksRepository;
        this.bookFormatParser = bookFormatParser;
        this.quoteCache = new LinkedList<>();
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    @Override
    @Transactional
    public void save(Quote quote) {
        log.debug("saving book");
        quotesRepository.save(quote);
        log.info("save book {}", quote);
    }

    @Override
    public Quote findOne(Long id) {
        log.debug("find quote by id {}", id);
        Optional<Quote> foundQuote = quotesRepository.findById(id);
        log.info("find quote {}", foundQuote);
        return foundQuote.orElse(null);
    }

    @Override
    public List<Quote> findAll() {
        log.debug("find all quotes");
        return quotesRepository.findAll();
    }

    @Override
    @Transactional
    public void update(Long id, Quote updatedQuote) {
        log.debug("update book by id {}", id);
        updatedQuote.setId(id);
        quotesRepository.save(updatedQuote);
        log.info("update quote {}", updatedQuote);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("delete quote by id {}", id);
        quotesRepository.deleteById(id);
        log.info("delete quote with id {} perform", id);
    }

    public void populateCache() {
        log.debug("Populating cache...");

        //get list of books from DB
        List<Book> allBooks = getAllActiveBooks();

        //get map of books with the number of identical
        Map<Book, Integer> parsedBooks = selectBooksRandomly(allBooks);

        //parse books and generate quotes
        generateQuotes(parsedBooks);

        log.debug("Cache populated successfully.");
    }

    private void generateQuotes(Map<Book, Integer> parsedBooks) {
        Random random = new Random();

        // Generate quotes for selected books
        for (Book book : parsedBooks.keySet()) {
            log.debug("Generating quotes for book: {}", book);

            Integer occurrences = parsedBooks.get(book);
            log.debug("Occurrences: {}", occurrences);

            String bookText = getBookText(book);

            List<String> words = Arrays.asList(bookText.split("\\s+"));

            do {
                String quoteContent;

                if (words.size() > 500) {
                    int startIndex = random.nextInt(words.size() - 500);
                    quoteContent = String.join(" ", words.subList(startIndex, startIndex + 500));
                } else {
                    quoteContent = bookText;
                }

                log.debug("quoteContent = {}", quoteContent);

                Quote quote = new Quote();
                quote.setBookSource(book);
                quote.setContent(quoteContent);
                quoteCache.offer(quote);

                occurrences--;
            } while (occurrences > 0);
        }

    }

    private String getBookText(Book book) {
        String bookText = bookFormatParser.parse(book);
        if (bookText == null) {
            log.error("Text from book {} is null!", book);
            throw new ServiceException("Text from book is null!");
        }
        return bookText;
    }

    private Map<Book, Integer> selectBooksRandomly(List<Book> allBooks) {
        Map<Book, Integer> parsedBooks = new HashMap<>();
        int counter = 0;
        Random random = new Random();

        // Randomly select books and track their occurrences
        while (counter < cacheSize) {
            Book randomBook = allBooks.get(random.nextInt(allBooks.size()));
            log.debug("Selected book: {}", randomBook);

            // Check if the book is already in the map
            Integer count = parsedBooks.getOrDefault(randomBook, 0);
            parsedBooks.put(randomBook, count + 1); // Increment count by 1
            counter++;
        }
        return parsedBooks;
    }

    private List<Book> getAllActiveBooks() {
        List<Book> allBooks = booksRepository.findByStatus(Status.ACTIVE);
        log.debug("Active books: {}", allBooks);
        if (allBooks.isEmpty()) {
            log.error("No active books available.");
            throw new ServiceException("No active books available.");
        }
        return allBooks;
    }

    public Quote getNextQuote() {
        if (quoteCache.isEmpty()) {
            populateCache();
        }
        return quoteCache.poll();
    }

    public void updateQuoteStatus(Quote quote, boolean isSuitable) {
        // TODO: Implement logic to update the status of the quote based on client feedback
    }
}
