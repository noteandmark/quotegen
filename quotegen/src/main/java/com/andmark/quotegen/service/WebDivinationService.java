package com.andmark.quotegen.service;

import jakarta.servlet.http.HttpSession;

public interface WebDivinationService {
    boolean checkPerformedDivination(HttpSession session);

    void markUserAsPerformedDivinationSession(HttpSession session);
}
