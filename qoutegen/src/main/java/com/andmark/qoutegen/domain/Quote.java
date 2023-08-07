package com.andmark.qoutegen.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "quote")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
//    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "usedAt")
    private Date usedAt;

    @ManyToOne
    // Join with the Book entity using "book_id" column, and it is mandatory (not nullable)
    @JoinColumn(name = "book_id", nullable = false, referencedColumnName = "id")
    private Book bookSource;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quote quote = (Quote) o;
        return Objects.equals(id, quote.id) && Objects.equals(content, quote.content) && Objects.equals(usedAt, quote.usedAt) && Objects.equals(bookSource, quote.bookSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, content, usedAt, bookSource);
    }
}
