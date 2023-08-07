package com.andmark.qoutegen.service.impl;

import com.andmark.qoutegen.domain.Book;
import com.andmark.qoutegen.domain.enums.Status;
import com.andmark.qoutegen.repository.BooksRepository;
import com.andmark.qoutegen.service.BooksService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@Slf4j
public class BooksServiceImpl implements BooksService {

    private final BooksRepository booksRepository;
    private final ModelMapper mapper;

    public BooksServiceImpl(BooksRepository booksRepository, ModelMapper mapper) {
        this.booksRepository = booksRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(Book book) {
        log.debug("saving book");
        booksRepository.save(book);
        log.info("save book {}", book);
    }

    @Override
    public Book findOne(Long id) {
        log.debug("find book by id {}", id);
        Optional<Book> foundBook = booksRepository.findById(id);
        log.info("find book {}", foundBook);
        return foundBook.orElse(null);
    }

    @Override
    public List<Book> findAll() {
        log.debug("find all books");
        return booksRepository.findAll();
    }

    @Override
    @Transactional
    public void update(Long id, Book updatedBook) {
        log.debug("update book by id {}", id);
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
}
