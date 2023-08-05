package com.andmark.qoutegen.model;

import com.andmark.qoutegen.model.enums.BookFormat;
import com.andmark.qoutegen.model.enums.Status;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "book")
@Data
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "author", nullable = false)
    private String author;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false)
    private BookFormat format;

    @Column(name = "filePath", nullable = false)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "DEFAULT 'ACTIVE'")
    private Status status;

    // "book" refers to the field name in the Quote (or Paragraph) entity that maps back to this Book entity
    @OneToMany(mappedBy = "bookSource", fetch = FetchType.LAZY)
    private List<Quote> quotes;

}
