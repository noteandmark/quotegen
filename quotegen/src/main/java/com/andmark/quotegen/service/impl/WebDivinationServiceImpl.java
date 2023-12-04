package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.service.WebDivinationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WebDivinationServiceImpl implements WebDivinationService {
    private final HttpSession httpSession;

    @Autowired
    public WebDivinationServiceImpl(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    @Override
    public boolean checkPerformedDivination(HttpSession session) {
        // Check if the attribute exists in the session
        return session.getAttribute("divinationPerformed") != null;
    }

    @Override
    public void markUserAsPerformedDivinationSession(HttpSession session) {
        // Set the attribute in the session
        session.setAttribute("divinationPerformed", true);
    }

}
