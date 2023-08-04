package com.andmark.qoutegen.service.impl;

import com.andmark.qoutegen.model.Book;
import com.andmark.qoutegen.repository.BooksRepository;
import com.andmark.qoutegen.service.BooksService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
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
        booksRepository.save(book);
    }

    @Override
    public Book findOne(Long id) {
        Optional<Book> foundBook = booksRepository.findById(id);
        return foundBook.orElse(null);
    }

    @Override
    public List<Book> findAll() {
        return booksRepository.findAll();
    }

    @Override
    @Transactional
    public void update(Long id, Book updatedBook) {
        updatedBook.setId(id);
        booksRepository.save(updatedBook);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        booksRepository.deleteById(id);
    }
}
