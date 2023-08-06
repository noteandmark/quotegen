package com.andmark.qoutegen.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Table(name = "quote")
@RequiredArgsConstructor
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
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "usedAt", nullable = false)
    private Date usedAt;

    @ManyToOne
    // Join with the Book entity using "book_id" column, and it is mandatory (not nullable)
    @JoinColumn(name = "book_id", nullable = false, referencedColumnName = "id")
    private Book bookSource;

}
