package com.andmark.qoutegen.domain;

import com.andmark.qoutegen.domain.enums.BookFormat;
import com.andmark.qoutegen.domain.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "book")
@AllArgsConstructor
@NoArgsConstructor
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id) && Objects.equals(title, book.title) && Objects.equals(author, book.author) && format == book.format && Objects.equals(filePath, book.filePath) && status == book.status && Objects.equals(quotes, book.quotes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, author, format, filePath, status, quotes);
    }
}
