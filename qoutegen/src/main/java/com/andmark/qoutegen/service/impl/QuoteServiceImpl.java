package com.andmark.qoutegen.service.impl;

import com.andmark.qoutegen.domain.Book;
import com.andmark.qoutegen.domain.Quote;
import com.andmark.qoutegen.domain.enums.Status;
import com.andmark.qoutegen.dto.QuoteDTO;
import com.andmark.qoutegen.exception.ServiceException;
import com.andmark.qoutegen.repository.BooksRepository;
import com.andmark.qoutegen.repository.QuotesRepository;
import com.andmark.qoutegen.service.QuoteService;
import com.andmark.qoutegen.util.BookFormatParser;
import com.andmark.qoutegen.util.BookFormatParserFactory;
import com.andmark.qoutegen.util.impl.MapperConvert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class QuoteServiceImpl implements QuoteService {
    @Value("${quote.cache.size:30}")
    private int cacheSize;
    private final Queue<Quote> quoteCache;

    private final QuotesRepository quotesRepository;
    private final BooksRepository booksRepository;
    private final BookFormatParserFactory bookFormatParserFactory;
    private final MapperConvert<Quote, QuoteDTO> mapper;

    @Autowired
    public QuoteServiceImpl(QuotesRepository quotesRepository, BooksRepository booksRepository, BookFormatParserFactory bookFormatParserFactory, MapperConvert<Quote, QuoteDTO> mapper) {
        this.quotesRepository = quotesRepository;
        this.booksRepository = booksRepository;
        this.bookFormatParserFactory = bookFormatParserFactory;
        this.mapper = mapper;
        this.quoteCache = new LinkedList<>();
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    @Override
    @Transactional
    public void save(QuoteDTO quoteDTO) {
        log.debug("saving quote");
        quotesRepository.save(convertToEntity(quoteDTO));
        log.info("save quote {}", quoteDTO);
    }

    @Override
    public QuoteDTO findOne(Long id) {
        log.debug("find quote by id {}", id);
        Optional<Quote> foundQuote = quotesRepository.findById(id);
        log.info("find quote {}", foundQuote);
        return foundQuote.map(this::convertToDTO).orElse(null);
    }

    @Override
    public List<QuoteDTO> findAll() {
        log.debug("find all quotes");
        List<Quote> quoteList = quotesRepository.findAll();
        log.info("founded quoteList = {}", quoteList);
        return quoteList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void update(Long id, QuoteDTO updatedQuoteDTO) {
        log.debug("update quote by id {}", id);
        Quote updatedQuote = convertToEntity(updatedQuoteDTO);
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

    @Transactional
    public void checkAndPopulateCache() {
        log.debug("service checkAndPopulateCache");
        int usedQuotesCount = quotesRepository.countByUsedAtIsNull();
        log.debug("usedQuotesCount is {}", usedQuotesCount);

        if (usedQuotesCount < 5) {
            log.debug("usedQuotesCount < 5");
            populateCache(cacheSize);
        }
    }

    public QuoteDTO provideQuoteToClient() {
        log.debug("service provideQuoteToClient");
        Quote quote = quotesRepository.findFirstByUsedAtIsNull();
        log.info("provideQuoteToClient = {}", quote);

        return convertToDTO(quote);
    }

    @Transactional
    public void confirmQuote(Long id, String content) {
        log.debug("service confirmQuote");
        Quote quote = quotesRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Quote not found with id: " + id));
        quote.setUsedAt(new Date());
        quote.setContent(content);
        log.info("setUsedAt date: " + quote.getUsedAt());
        quotesRepository.save(quote);
        log.info("quote with id = {} saved in database", quote.getId());
    }

    @Override
    @Transactional
    public void populateCache(Integer cacheSize) {
        int size = (cacheSize != null && cacheSize > 0) ? cacheSize : this.cacheSize;
        log.debug("Populating cache with quotes (cacheSize = {})", size);
        //get list of books from DB
        List<Book> allBooks = getAllActiveBooks();

        //get map of books with the number of identical
        Map<Book, Integer> parsedBooks = selectBooksRandomly(allBooks, size);

        //parse books and generate quotes
        generateQuotes(parsedBooks);
        // Save quotes in the cache to the database
        saveQuotesFromCache(quoteCache);

        log.debug("Cache populated successfully.");
    }

    public List<Book> getAllActiveBooks() {
        List<Book> allBooks = booksRepository.findByStatus(Status.ACTIVE);
        log.debug("Active books: {}", allBooks);
        if (allBooks.isEmpty()) {
            log.error("No active books available.");
            throw new ServiceException("No active books available.");
        }
        return allBooks;
    }

    private Map<Book, Integer> selectBooksRandomly(List<Book> allBooks, int size) {
        Map<Book, Integer> parsedBooks = new HashMap<>();
        int counter = 0;
        Random random = new Random();

        // Randomly select books and track their occurrences
        while (counter < size) {
            Book randomBook = allBooks.get(random.nextInt(allBooks.size()));
            log.debug("Selected book: {}", randomBook);

            // Check if the book is already in the map
            Integer count = parsedBooks.getOrDefault(randomBook, 0);
            parsedBooks.put(randomBook, count + 1); // Increment count by 1
            counter++;
        }
        return parsedBooks;
    }

    private void generateQuotes(Map<Book, Integer> parsedBooks) {
        // Generate quotes for selected books
        for (Book book : parsedBooks.keySet()) {
            log.debug("Generating quotes for book: {}", book);

            Integer occurrences = parsedBooks.get(book);
            log.debug("Occurrences: {}", occurrences);

            String bookText = getBookText(book);

            parseAndCacheQuotes(book, bookText, occurrences);
        }

    }

    @Transactional
    public void saveQuotesFromCache(Queue<Quote> quoteCache) {
        List<Quote> quotesToSave = getQuotes(quoteCache);

        if (!quotesToSave.isEmpty()) {
            log.debug("Saving quotes to the database: {}", quotesToSave);
            quotesRepository.saveAll(quotesToSave);
            log.info("Quotes saved, size: {}", quotesToSave.size());
        } else {
            log.error("nothing to save, something wrong");
        }
    }

    private List<Quote> getQuotes(Queue<Quote> quoteCache) {
        List<Quote> quotesToSave = new ArrayList<>();
        while (!quoteCache.isEmpty()) {
            Quote quote = quoteCache.poll();
            if (quote != null) {
                log.debug("quote to save to list with id = {}", quote.getId());
                quotesToSave.add(quote);
            }
        }
        return quotesToSave;
    }

    private void parseAndCacheQuotes(Book book, String bookText, Integer occurrences) {
        List<String> words = Arrays.asList(bookText.split("\\s+"));
        do {
            String quoteContent = generateQuoteContent(words);
            log.debug("quoteContent = {}", quoteContent);

            Quote quote = new Quote();
            quote.setBookSource(book);
            quote.setContent(quoteContent);
            quoteCache.offer(quote);

            occurrences--;
        } while (occurrences > 0);
    }

    public String generateQuoteContent(List<String> words) {
        Random random = new Random();

        if (words.size() <= 500) {
            return String.join(" ", words);
        } else {
            int startIndex = random.nextInt(words.size() - 500);
            return String.join(" ", words.subList(startIndex, startIndex + 500));
        }
    }

    public String getBookText(Book book) {
        // "Factory Object" variation of the Factory Method pattern, where a separate class is responsible for creating objects
        BookFormatParser parser = bookFormatParserFactory.createParser(book.getFormat());
        String bookText = parser.parse(book);
        if (bookText == null) {
            log.error("Text from book {} is null!", book);
            throw new ServiceException("Text from book is null!");
        }
        return bookText;
    }

    public Quote getNextQuote() {
        if (quoteCache.isEmpty()) {
            populateCache(cacheSize);
        }
        Quote pollQuote = quoteCache.poll();
        log.info("get NextQuote quote: {}", pollQuote);
        return pollQuote;
    }

    public QuoteDTO convertToDTO(Quote quote) {
        return mapper.convertToDTO(quote, QuoteDTO.class);
    }

    public Quote convertToEntity(QuoteDTO quoteDTO) {
        return mapper.convertToEntity(quoteDTO, Quote.class);
    }
}
