package com.andmark.quotegen.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendEmail(String toEmail, String subject, String text) throws MessagingException;
}
