package com.andmark.quotegen.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "greeting")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Greeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;
}
