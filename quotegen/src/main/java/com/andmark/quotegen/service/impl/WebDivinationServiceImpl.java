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


//    public DivinationResultDTO performDivination(User user, int pageNumber, int lineNumber) {
//        if (hasUserPerformedDivinationToday(user)) {
//            return createAlreadyPerformedResult();
//        }
//
//        // ... логика проведения гадания ...
//
//        markUserAsPerformedDivinationToday(user);
//
//        // ... логика формирования результата гадания ...
//
//        return createDivinationResult();
//    }
//
//    private boolean hasUserPerformedDivinationToday(User user) {
//        LocalDate lastDivinationDate = (LocalDate) httpSession.getAttribute(getLastDivinationDateKey(user));
//        return lastDivinationDate != null && lastDivinationDate.equals(LocalDate.now());
//    }
//
//    private void markUserAsPerformedDivinationToday(User user) {
//        httpSession.setAttribute(getLastDivinationDateKey(user), LocalDate.now());
//    }
//
//    private String getLastDivinationDateKey(User user) {
//        return "lastDivinationDate_" + user.getId();
//    }
//
//    private DivinationResultDTO createAlreadyPerformedResult() {
//        DivinationResultDTO result = new DivinationResultDTO();
//        result.setMessage("Вы уже делали гадание сегодня. Попробуйте завтра снова.");
//        return result;
//    }
//
//    private DivinationResultDTO createDivinationResult() {
//        // ... логика формирования результата гадания ...
//        return new DivinationResultDTO();
//    }
