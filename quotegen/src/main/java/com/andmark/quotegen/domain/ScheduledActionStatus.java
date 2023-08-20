package com.andmark.quotegen.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_status")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ScheduledActionStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "last_executed")
    private LocalDateTime lastExecuted;

}
