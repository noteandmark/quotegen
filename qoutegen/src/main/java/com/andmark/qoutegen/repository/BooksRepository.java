package com.andmark.qoutegen.repository;

import com.andmark.qoutegen.model.Book;
import com.andmark.qoutegen.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BooksRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByFilePath(String filePath);

    List<Book> findByStatus(Status status);
}
