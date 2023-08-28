package com.andmark.quotebot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailableDaysResponseDTO {
    private LocalDateTime availableDay;
    private String message;
}
