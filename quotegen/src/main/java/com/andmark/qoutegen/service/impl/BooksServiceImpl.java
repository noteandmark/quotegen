package com.andmark.qoutegen.service.impl;

import com.andmark.qoutegen.domain.Book;
import com.andmark.qoutegen.domain.enums.Status;
import com.andmark.qoutegen.dto.BookDTO;
import com.andmark.qoutegen.repository.BooksRepository;
import com.andmark.qoutegen.service.BooksService;
import com.andmark.qoutegen.util.impl.MapperConvert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class BooksServiceImpl implements BooksService {

    private final BooksRepository booksRepository;
    private final MapperConvert<Book, BookDTO> mapper;

    @Autowired
    public BooksServiceImpl(BooksRepository booksRepository, MapperConvert<Book, BookDTO> mapper) {
        this.booksRepository = booksRepository;
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
        log.info("founded bookList = {}", bookList);
        return bookList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
        List<Book> deletedBooks = booksRepository.findByStatus(Status.DELETED);
        log.debug("books that will be deleted: " + deletedBooks);
        booksRepository.deleteAll(deletedBooks);
        log.info("service clearDeletedBooks perform");
    }

    public BookDTO convertToDTO(Book book) {
        return mapper.convertToDTO(book, BookDTO.class);
    }

    public Book convertToEntity(BookDTO bookDTO) {
        return mapper.convertToEntity(bookDTO, Book.class);
    }
}
