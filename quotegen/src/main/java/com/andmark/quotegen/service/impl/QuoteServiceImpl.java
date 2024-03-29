package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.domain.Quote;
import com.andmark.quotegen.domain.User;
import com.andmark.quotegen.domain.enums.BookStatus;
import com.andmark.quotegen.domain.enums.QuoteStatus;
import com.andmark.quotegen.dto.AvailableDayResponseDTO;
import com.andmark.quotegen.dto.QuoteDTO;
import com.andmark.quotegen.dto.UserDTO;
import com.andmark.quotegen.exception.NotFoundBookException;
import com.andmark.quotegen.exception.ServiceException;
import com.andmark.quotegen.repository.BooksRepository;
import com.andmark.quotegen.repository.QuotesRepository;
import com.andmark.quotegen.service.QuoteService;
import com.andmark.quotegen.service.UserService;
import com.andmark.quotegen.util.BookFormatParser;
import com.andmark.quotegen.util.BookFormatParserFactory;
import com.andmark.quotegen.util.MapperConvert;
import jakarta.persistence.EntityNotFoundException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.andmark.quotegen.config.AppConfig.*;

@Service
@Transactional(readOnly = true)
@Slf4j
public class QuoteServiceImpl implements QuoteService {
    @Setter
    @Value("${quote.cache.size:30}")
    private int cacheSize;

    private final Queue<Quote> quoteCache;

    private final UserService userService;
    private final QuotesRepository quotesRepository;
    private final BooksRepository booksRepository;
    private final BookFormatParserFactory bookFormatParserFactory;
    private final MapperConvert<Quote, QuoteDTO> mapper;

    @Autowired
    public QuoteServiceImpl(UserService userService, QuotesRepository quotesRepository, BooksRepository booksRepository, BookFormatParserFactory bookFormatParserFactory, MapperConvert<Quote, QuoteDTO> mapper) {
        this.userService = userService;
        this.quotesRepository = quotesRepository;
        this.booksRepository = booksRepository;
        this.bookFormatParserFactory = bookFormatParserFactory;
        this.mapper = mapper;
        this.quoteCache = new LinkedList<>();
    }

    @Override
    @Transactional
    public QuoteDTO save(QuoteDTO quoteDTO) {
        log.debug("quote service: save");
        log.debug("quoteDTO = {}", quoteDTO);
        Quote quote = convertToEntity(quoteDTO);
        log.debug("quote = {}", quote);
        Long bookId = quoteDTO.getBookId();
        if (bookId != null) {
            log.debug("looking a bookSource for bookId = {}", bookId);
            Optional<Book> bookSource = booksRepository.findById(bookId);
            if (bookSource.isPresent()) {
                log.debug("set bookSource");
                quote.setBookSource(bookSource.get());
            } else throw new NotFoundBookException("book with id = " + bookId + "not found");
        }
        // user-suggested quote
        else {
            log.debug("user-suggested quote");

        }
        quotesRepository.save(quote);
        log.info("saved quote with id = {}", quote.getId());
        return convertToDTO(quote);
    }

    @Override
    @Transactional
    // add suggest quote from web
    public void suggestQuote(QuoteDTO quoteDTO, String username) {
        log.debug("quote service: suggestQuote from web");

        UserDTO userDTO = userService.findByUsername(username);
        log.debug("");
        User user = userService.convertToEntity(userDTO);
        log.debug("user = {}", user);
        quoteDTO.setUserId(user.getId());

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("Предложено пользователем : ").append(user.getNickname()).append("\n");
        contentBuilder.append("Цитата: ").append(quoteDTO.getContent()).append("\n");
        contentBuilder.append("Книга: ").append(quoteDTO.getBookTitle()).append("\n");
        contentBuilder.append("Автор: ").append(quoteDTO.getBookAuthor());
        quoteDTO.setContent(contentBuilder.toString());
        quoteDTO.setStatus(QuoteStatus.FREE);

        save(quoteDTO);
    }

    @Override
    @Transactional
    // add suggest quote from telegram-bot
    public void addSuggestedQuote(QuoteDTO quoteDTO) {
        log.debug("quote service: addSuggestedQuote from telegram-bot");
        save(quoteDTO);
    }

    @Override
    public QuoteDTO findOne(Long id) {
        log.debug("quote service: findOne quote by id {}", id);
        Optional<Quote> foundQuote = quotesRepository.findById(id);
        log.info("found quote with id = {}", foundQuote.map(Quote::getId).orElse(null));
        return foundQuote.map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public List<QuoteDTO> findAll() {
        log.debug("quote service: find all quotes");
        List<Quote> quoteList = quotesRepository.findAll();
        log.info("founded quoteList.size = {}", quoteList.size());
        return convertToDtoList(quoteList);
    }

    @Override
    public Page<QuoteDTO> findAllSorted(Pageable pageable, String sortField, String sortDirection) {
        log.debug("quote service: find all quotes sorted");
        Page<Quote> quotePage;

        if ("bookAuthor".equals(sortField) && sortDirection != null) {
            // Use the custom query for sorting by bookAuthor
            log.debug("get sorting by field bookAuthor");
            quotePage = sortBookAuthorQuotes(pageable, sortDirection);
        }
        else if ("bookTitle".equals(sortField) && sortDirection != null) {
            // Use the custom query for sorting by bookTitle
            log.debug("get sorting by field bookTitle");
            quotePage = sortBookTitleQuotes(pageable, sortDirection);
        }
        else {
            // Use the default query for other fields
            log.debug("get sorting by field = {}", sortField);
            pageable = configureDefaultSorting(pageable, sortField, sortDirection);
            quotePage = quotesRepository.findAll(pageable);
        }

        Page<QuoteDTO> quoteDTOPage = convertToQuoteDTOPage(quotePage, pageable);
        log.debug("founded quoteDTOPage.count = {}", quoteDTOPage.stream().count());

        return quoteDTOPage;
    }

    @Override
    @Transactional
    public QuoteDTO update(QuoteDTO updatedQuoteDTO) {
        log.debug("quote service: update quote by id {}", updatedQuoteDTO.getId());
        Long bookId = updatedQuoteDTO.getBookId();
        log.debug("bookId in updatedQuoteDTO= {}", bookId);
        Quote updatedQuote = convertToEntity(updatedQuoteDTO);
        Book book = booksRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));
        updatedQuote.setBookSource(book);

        quotesRepository.save(updatedQuote);
        log.info("service: updated quote");
        return updatedQuoteDTO;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("quote service: delete quote by id {}", id);
        quotesRepository.deleteById(id);
        log.info("delete quote with id {} perform", id);
    }

    @Override
    @Transactional
    public void checkAndPopulateCache() {
        log.debug("quote service: checkAndPopulateCache");
        int unusedQuotesCount = quotesRepository.countByUsedAtIsNullAndPendingTimeIsNull();
        log.debug("usedQuotesCount is {}", unusedQuotesCount);

        if (unusedQuotesCount < 5) {
            log.debug("unusedQuotesCount < 5");
            populateCache(cacheSize);
        }
    }

    @Override
    public QuoteDTO provideQuoteToClient() {
        log.debug("quote service: provideQuoteToClient");
        Quote quote = quotesRepository.findFirstByUsedAtIsNullAndPendingTimeIsNullOrderByIdAsc();
        log.info("provideQuoteToClient id = {}", quote.getId());
        return convertToDTO(quote);
    }

    @Override
    @Transactional
    public void pendingQuote(QuoteDTO quoteDTO) {
        log.debug("quote service: pendingQuote");
        Quote quote = quotesRepository.findById(quoteDTO.getId())
                .orElseThrow(() -> new ServiceException("Quote not found with id: " + quoteDTO.getId()));
        quote.setPendingTime(quoteDTO.getPendingTime());
        quote.setContent(quoteDTO.getContent());
        quote.setImageUrl(quoteDTO.getImageUrl());
        quote.setStatus(quoteDTO.getStatus());
        log.info("setPendingTime date: " + quote.getPendingTime());
        quotesRepository.save(quote);
        log.info("quote with id = {} saved in database", quote.getId());
    }

    @Override
    public QuoteDTO getRandomPublishedQuote() {
        log.debug("quote service: getRandomPublishedQuote");
        List<Quote> publishedQuotes = quotesRepository.findByStatus(QuoteStatus.PUBLISHED);

        if (!publishedQuotes.isEmpty()) {
            Quote randomQuote = getRandomElement(publishedQuotes);
            log.debug("return randomQuote with id = {}", randomQuote.getId());
            return convertToDTO(randomQuote);
        } else {
            log.debug("there were no quotes from stasus PUBLISHED in database");
            return null;
        }
    }

    @Override
    public List<QuoteDTO> getPublishedQuotesForWeek() {
        log.debug("quote service: getPublishedQuotesForWeek");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.with(DayOfWeek.MONDAY).with(LocalTime.MIN);
        LocalDateTime endOfWeek = now.with(DayOfWeek.SUNDAY).with(LocalTime.MAX);

        List<Quote> quotes = quotesRepository.findByStatusAndUsedAtBetween(
                QuoteStatus.PUBLISHED, startOfWeek, endOfWeek);
        log.debug("quote service: get quotes for week in size: {}", quotes.size());

        return convertToDtoList(quotes);
    }

    @Override
    @Transactional
    public void confirmQuote(QuoteDTO quoteDTO) {
        log.debug("quote service: confirmQuote");
        Quote quote = quotesRepository.findById(quoteDTO.getId())
                .orElseThrow(() -> new ServiceException("Quote not found with id: " + quoteDTO.getId()));
        LocalDateTime currentDateTime = LocalDateTime.now(); // Get current date and time
        quote.setUsedAt(currentDateTime);
        quote.setContent(quoteDTO.getContent());
        quote.setImageUrl(quoteDTO.getImageUrl());
        quote.setStatus(quoteDTO.getStatus());
        log.info("setUsedAt date: " + quote.getUsedAt());
        quotesRepository.save(quote);
        log.info("quote with id = {} saved in database", quote.getId());
    }

    @Override
    @Transactional
    public void rejectQuote(Long id) {
        log.debug("quote service: reject quote with id = {}", id);
        QuoteDTO existingQuote = findOne(id);
        log.debug("existingQuote = {}", existingQuote);

        if (existingQuote != null) {
            log.debug("service rejected quote id = {}", id);
            delete(id);
            log.info("service deleted quote id = {}", id);
        } else {
            log.warn("service couldn't find the quote with id = {}", id);
            throw new EntityNotFoundException("Quote with id " + id + " not found");
        }
    }

    @Override
    @Transactional
    public void populateCache(Integer cacheSize) {
        log.debug("quote service: populateCache");
        int size = (cacheSize != null && cacheSize > 0) ? cacheSize : this.cacheSize;
        log.debug("Populating cache with quotes (cacheSize = {})", size);
        //get list of books from DB
        Long bookCount = booksRepository.count();
        log.debug("bookCount = {}", bookCount);
        if (bookCount == 0) {
            log.warn("No books! Scan the catalogue first.");
            throw new NotFoundBookException("No books! Scan the catalogue first.");
        }
        List<Book> allBooks = getAllActiveBooks();

        //get map of books with the number of identical
        Map<Book, Integer> parsedBooks = selectBooksRandomly(allBooks, size);
        //parse books and generate quotes
        generateQuotes(parsedBooks);
        // Save quotes in the cache to the database and clear cache
        saveQuotesFromCache(quoteCache);
        quoteCache.clear();
        log.info("Cache populated successfully.");
    }

    public List<Book> getAllActiveBooks() {
        log.debug("quote service: getAllActiveBooks from repository");
        List<Book> allBooks = booksRepository.findByBookStatus(BookStatus.ACTIVE);
        log.debug("Active books: {}", allBooks);
        if (allBooks.isEmpty()) {
            log.error("No active books available.");
            throw new ServiceException("No active books available.");
        }
        return allBooks;
    }

    @Override
    public List<QuoteDTO> getPendingQuotes() {
        log.debug("quote service: getPendingQuotes");
        List<Quote> pendingQuoteEntities = quotesRepository.findByStatus(QuoteStatus.PENDING);
        return convertToDtoList(pendingQuoteEntities);
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

    @Transactional
    public void saveQuotesFromCache(Queue<Quote> quoteCache) {
        log.debug("quote service: saveQuotesFromCache");
        List<Quote> quotesToSave = getQuotes(quoteCache);

        if (!quotesToSave.isEmpty()) {
            log.debug("Saving quotes to the database: {}", quotesToSave);
            quotesRepository.saveAll(quotesToSave);
            log.info("Quotes saved, size: {}", quotesToSave.size());
        } else {
            log.error("nothing to save, something wrong");
        }
    }

    @Override
    public String getBookText(Book book) {
        log.debug("quoteService: getting text from book with id = {}, format = {}", book.getId(), book.getFormat());
        // "Factory Object" variation of the Factory Method pattern, where a separate class is responsible for creating objects
        BookFormatParser parser = bookFormatParserFactory.createParser(book.getFormat());
        String rawBookText;
        rawBookText = parser.parse(book); // Get the raw text from the parser
        if (rawBookText == null) {
            log.error("Text is null from book {}", book);
            return "";
        }
        log.debug("rawBookText length = {}", rawBookText.length());
        // Fix formatting by adding spaces after punctuation marks if there is none
        String bookText = fixFormatting(rawBookText);
        log.debug("bookText length = {}", bookText.length());
        return bookText;
    }

    @Override
    public AvailableDayResponseDTO getAvailableDays() {
        log.debug("quote service: getAvailableDays");
        AvailableDayResponseDTO availableDayResponseDTO = new AvailableDayResponseDTO();
        StringBuilder messageText = new StringBuilder();

        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime beginningOfDay = currentDateTime.toLocalDate().plusDays(1).atStartOfDay();
        log.debug("beginningOfDay = {}", beginningOfDay);
        LocalDateTime startDate = beginningOfDay; // Start from tomorrow
        log.debug("startDate = {}", startDate);
        LocalDateTime endDate = startDate.plusDays(daysPublicationAhead); // n days in advance
        log.debug("endDate = {}", endDate);
        // the maximum end date based on maxPeriodDaysAhead and beginningOfDay
        LocalDateTime maxEndDate = beginningOfDay.plusDays(maxPeriodDaysAhead);
        log.debug("maxEndDate = {}", maxEndDate);

        boolean foundAvailableDay = false;
        Random random = new Random();
        // searching until we find a free day to publish, or go beyond the maximum period
        do {
            log.debug("sending request to database");
            // a list of all days between startDate and endDate
            List<LocalDate> allDaysInRange = generateDateRange(startDate.toLocalDate(), endDate.minusMinutes(1).toLocalDate());
            // Initialize the dayToCountMap with 0 counts for all days in the range
            System.out.println("allDaysInRange = " + allDaysInRange);
            Map<LocalDate, Long> dayToCountMap = allDaysInRange.stream()
                    .collect(Collectors.toMap(day -> day, day -> 0L));

            // Fetch pending times between startTime and endTime
            List<LocalDateTime> pendingTimes = quotesRepository.findPendingTimesBetween(startDate, endDate);
            log.debug("get pendingTimes size: {}", pendingTimes.size());

            // Populate the dayToCountMap with actual counts from pendingTimes
            pendingTimes.stream()
                    .map(LocalDateTime::toLocalDate)
                    .forEach(day -> dayToCountMap.put(day, dayToCountMap.getOrDefault(day, 0L) + 1));

            // Filter days with no more than 2 occurrences of pending times
            List<LocalDate> availableDays = dayToCountMap.entrySet().stream()
                    .filter(entry -> entry.getValue() < QUOTES_IN_DAY)
                    .map(Map.Entry::getKey)
                    .toList();
            log.debug("availableDays in size: {}", availableDays.size());

            if (!availableDays.isEmpty()) {
                log.debug("availableDays is not empty");
                foundAvailableDay = true;
                LocalDate selectedDay = availableDays.get(random.nextInt(availableDays.size()));
                log.debug("getting selectedDay: {}", selectedDay);

                LocalDateTime scheduledDate = generateRandomTime(selectedDay);
                log.debug("return random date+time = {}", scheduledDate);

                messageText.append("Выбрана случайная дата публикации.");
                availableDayResponseDTO.setAvailableDay(scheduledDate);
                availableDayResponseDTO.setMessage(messageText.toString());
                return availableDayResponseDTO;
            }
            // in the specified period all days are filled
            else {
                log.debug("no any pending publications");
                // Extend the search window by another "daysPublicationAhead" days
                messageText.append("Нет свободных дней для отложенных публикаций до даты: ")
                        .append(endDate)
                        .append("\n")
                        .append("Переношу поиск на следующий период.")
                        .append("\n");
                startDate = endDate;
                // Calculate the new endDate based on daysPublicationAhead
                LocalDateTime newEndDate = endDate.plusDays(daysPublicationAhead);
                if (endDate.equals(maxEndDate)) {
                    foundAvailableDay = true;
                    log.debug("Didn't find any available days right up to the date: {}", maxEndDate);
                    messageText.setLength(0);
                    messageText.append("Назначьте дату вручную, всё заполнено. Не нашли свободных дней до даты: ").append(maxEndDate);
                    availableDayResponseDTO.setMessage(messageText.toString());
                }
                // Adjust the newEndDate to be the minimum of maxEndDate and the calculated newEndDate
                endDate = newEndDate.isBefore(maxEndDate) ? newEndDate : maxEndDate;
                log.debug("new startDate = {}, endDate = {}", startDate, endDate);
            }
        } while (!foundAvailableDay);
        return availableDayResponseDTO;
    }

    private LocalDateTime generateRandomTime(LocalDate date) {
        log.debug("quote service: generating random time");
        int hour = 8 + new Random().nextInt(16); // Random hour between 8 and 23
        int minute = new Random().nextInt(60);    // Random minute between 0 and 59
        log.debug("hour = {}, minute = {}", hour, minute);
        return LocalDateTime.of(date, LocalTime.of(hour, minute));
    }

    private List<LocalDate> generateDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("quote service: generating date range");
        List<LocalDate> dateRange = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            dateRange.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }

        return dateRange;
    }

    private String fixFormatting(String rawText) {
        log.debug("quote service: fixing formatting text");
        // Define a set of punctuation marks that need a space after them
        String[] punctuationMarks = {".", ",", "!", "?", ":", ";", "—"};

        // Iterate through the punctuation marks and add a space after each if needed
        for (String mark : punctuationMarks) {
            rawText = rawText.replace(mark, mark + " ");
        }

        // Remove any excessive spaces
        rawText = rawText.replaceAll("\\s+", " ");
        log.debug("return fixing text");
        return rawText.trim(); // Trim leading and trailing spaces
    }

    private Map<Book, Integer> selectBooksRandomly(List<Book> allBooks, int size) {
        log.debug("quote service: select books randomly");
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
        log.debug("quote service: generate quotes");
        // Generate quotes for selected books
        for (Book book : parsedBooks.keySet()) {
            log.debug("Generating quotes for book: {}", book);

            Integer occurrences = parsedBooks.get(book);
            log.debug("Occurrences: {}", occurrences);

            String bookText = getBookText(book);

            parseAndCacheQuotes(book, bookText, occurrences);
        }

    }

    private List<Quote> getQuotes(Queue<Quote> quoteCache) {
        log.debug("quote service: get quotes");
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
        log.debug("quote service: parse and cache quotes");
        List<String> words = Arrays.asList(bookText.split("\\s+"));
        do {
            String quoteContent = generateQuoteContent(words);
            log.debug("quoteContent = {}", quoteContent);

            Quote quote = new Quote();
            quote.setBookSource(book);
            quote.setContent(quoteContent);
            quote.setStatus(QuoteStatus.FREE);
            quoteCache.offer(quote);

            occurrences--;
        } while (occurrences > 0);
    }

    private Page<Quote> sortBookAuthorQuotes(Pageable pageable, String sortDirection) {
        if ("asc".equalsIgnoreCase(sortDirection)) {
            return quotesRepository.findAllSortedByBookAuthorByASC(pageable);
        } else if ("desc".equalsIgnoreCase(sortDirection)) {
            return quotesRepository.findAllSortedByBookAuthorByDESC(pageable);
        } else {
            throw new IllegalArgumentException("Invalid sort direction: " + sortDirection);
        }
    }

    private Page<Quote> sortBookTitleQuotes(Pageable pageable, String sortDirection) {
        if ("asc".equalsIgnoreCase(sortDirection)) {
            return quotesRepository.findAllSortedByBookTitleByASC(pageable);
        } else if ("desc".equalsIgnoreCase(sortDirection)) {
            return quotesRepository.findAllSortedByBookTitleByDESC(pageable);
        } else {
            throw new IllegalArgumentException("Invalid sort direction: " + sortDirection);
        }
    }

    private Pageable configureDefaultSorting(Pageable pageable, String sortField, String sortDirection) {
        if (sortField != null && sortDirection != null) {
            log.debug("get sorting by field = {}", sortField);
            Sort sort = Sort.by(sortDirection.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            log.debug("get pageable = {}", pageable);
        }
        return pageable;
    }

    private <T> T getRandomElement(List<T> list) {
        int randomIndex = new Random().nextInt(list.size());
        return list.get(randomIndex);
    }

    private Page<QuoteDTO> convertToQuoteDTOPage(Page<Quote> quotePage, Pageable pageable) {
        List<QuoteDTO> quoteList = quotePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(quoteList, pageable, quotePage.getTotalElements());
    }

    private QuoteDTO convertToDTO(Quote quote) {
        return mapper.convertToDTO(quote, QuoteDTO.class);
    }

    private Quote convertToEntity(QuoteDTO quoteDTO) {
        return mapper.convertToEntity(quoteDTO, Quote.class);
    }

    private List<QuoteDTO> convertToDtoList(List<Quote> quotes) {
        return quotes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
