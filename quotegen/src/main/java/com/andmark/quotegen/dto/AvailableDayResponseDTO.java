package com.andmark.quotegen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailableDayResponseDTO {
    private LocalDateTime availableDay;
    private String message;
}
