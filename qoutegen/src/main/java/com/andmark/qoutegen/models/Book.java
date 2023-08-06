package com.andmark.qoutegen.models;

import com.andmark.qoutegen.models.enums.BookFormat;
import com.andmark.qoutegen.models.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "book")
@RequiredArgsConstructor
@Getter
@Setter
@ToString
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
    @ToString.Exclude
    private List<Quote> quotes;

}
