package com.andmark.quotegen.repository;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.domain.enums.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BooksRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByFilePath(String filePath);

    List<Book> findByBookStatus(BookStatus bookStatus);
}
