package com.andmark.qoutegen.model;

import com.andmark.qoutegen.model.enums.BookFormat;
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

    private String title;
    private String author;

    @Enumerated(EnumType.STRING)
    private BookFormat format;

    private String filePath;

    // "book" refers to the field name in the Quote (or Paragraph) entity that maps back to this Book entity
    @OneToMany(mappedBy = "bookSource", fetch = FetchType.LAZY)
    private List<Quote> quotes;

}
