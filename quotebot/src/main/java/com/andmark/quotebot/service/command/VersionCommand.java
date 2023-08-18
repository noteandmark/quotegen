package com.andmark.quotebot.service.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.andmark.quotebot.config.BotConfig.changelogFile;
import static com.andmark.quotebot.config.BotConfig.readmeFile;

@Slf4j
public class VersionCommand extends QuoteCommand {

    public VersionCommand() {
        super("version", "Display current version and changelog");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {} execute /version", user.getId());
        String readmeContent = loadFileContent(readmeFile);
        String changelogContent = loadFileContent(changelogFile);

        if (!readmeContent.isEmpty() || !changelogContent.isEmpty()) {
            List<String> messages = splitContentIntoMessages(readmeContent + "\n\n" + changelogContent);

            if (!messages.isEmpty()) {
                for (String message : messages) {
                    sendMessage(absSender, chat, message);
                }
            } else {
                sendMessage(absSender, chat, "Содержание версий в настоящее время недоступно");
            }
        } else {
            sendMessage(absSender, chat, "В настоящее время readme и changelog недоступны.");
        }
    }

    private String loadFileContent(String resourcePath) {
        log.debug("try to load file content from file: {}", resourcePath);

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream != null) {
                byte[] bytes = inputStream.readAllBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            } else {
                log.error("Resource not found: {}", resourcePath);
                return "";
            }
        } catch (IOException e) {
            log.error("Error loading resource content", e);
            return "";
        }

//        ClassLoader cl = ClassLoader.getSystemClassLoader();
//        URL[] urls = ((URLClassLoader) cl).getURLs();
//        for (URL url : urls) {
//            System.out.println(url.getFile());
//        }

//        try {
//            ClassPathResource resource = new ClassPathResource(resourcePath);
//            byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
//            return new String(bytes, StandardCharsets.UTF_8);
//        } catch (IOException e) {
//            log.error("Error loading resource content", e);
//            return "";
//        }
    }

    // if needed loading from url address
    private String loadFileFromUrl(String filePath) {
        try {
            URL url = new URL(filePath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator());
            }
            reader.close();
            connection.disconnect();

            return stringBuilder.toString();
        } catch (IOException e) {
            log.error("Error loading file content: {}", e.getMessage());
            return "";
        }
    }

    private List<String> splitContentIntoMessages(String content) {
        int maxMessageLength = 4000;
        List<String> messages = new ArrayList<>();

        for (int startIndex = 0; startIndex < content.length(); startIndex += maxMessageLength) {
            int endIndex = Math.min(startIndex + maxMessageLength, content.length());
            String messagePart = content.substring(startIndex, endIndex);
            messages.add(messagePart);
        }




//        int maxMessageLength = 4000;
//        List<String> messages = new ArrayList<>();
//        StringBuilder currentMessage = new StringBuilder();
//        String[] lines = content.split("\\r?\\n");
//
//        for (String line : lines) {
//            if (currentMessage.length() + line.length() + 1 <= maxMessageLength) {
//                // Adding a line to the current message won't exceed the limit
//                if (currentMessage.length() > 0) {
//                    currentMessage.append("\n");
//                }
//                currentMessage.append(line);
//            } else {
//                // Start a new message and add the line
//                messages.add(currentMessage.toString());
//                currentMessage = new StringBuilder(line);
//            }
//        }
//
//        // Add the last message
//        if (currentMessage.length() > 0) {
//            messages.add(currentMessage.toString());
//        }



        //working code
        // int maxMessageLength = 4000;
        //        List<String> messages = new ArrayList<>();
//        StringBuilder currentMessage = new StringBuilder();
//        String[] lines = content.split("\\r?\\n");
//        for (String line : lines) {
//            if (currentMessage.length() + line.length() + 1 <= maxMessageLength) {
//                // Adding a line to the current message won't exceed the limit
//                if (currentMessage.length() > 0) {
//                    currentMessage.append("\n");
//                }
//                currentMessage.append(line);
//            } else {
//                // Start a new message and add the line
//                messages.add(currentMessage.toString());
//                currentMessage = new StringBuilder(line);
//            }
//        }
//        // Add the last message
//        if (currentMessage.length() > 0) {
//            messages.add(currentMessage.toString());
//        }

        return messages;
    }
}