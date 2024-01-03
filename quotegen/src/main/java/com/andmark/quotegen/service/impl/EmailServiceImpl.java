package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.service.EmailService;
import jakarta.mail.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    @Value("${spring.mail.username}")
    private String toEmail;
    @Value("${spring.mail.password}")
    private String emailPassword;

    @Override
    public void sendEmail(String fromEmail, String subject, String text) throws MessagingException {
        Properties properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(toEmail, emailPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setFrom(new InternetAddress());
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setText("Email from: " + fromEmail + "\n\n" + text);

        log.debug("set message");

        Transport.send(message);

        log.debug("send Transport");
    }
}
