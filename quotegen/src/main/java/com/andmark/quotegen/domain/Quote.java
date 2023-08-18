package com.andmark.quotegen.domain;

import com.andmark.quotegen.domain.enums.QuoteStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "DEFAULT 'FREE'")
    private QuoteStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "pending_time")
    private LocalDateTime pendingTime;

    @Column(name = "imageUrl")
    private String imageUrl;

    @Temporal(TemporalType.TIMESTAMP)
//  (it includes @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss"))
    @Column(name = "usedAt")
    private LocalDateTime usedAt;

    @ManyToOne
    // Join with the Book entity using "book_id" column, and it is mandatory (not nullable)
    @JoinColumn(name = "book_id", nullable = false, referencedColumnName = "id")
    @JsonIgnore // to prevent infinite recursion
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
