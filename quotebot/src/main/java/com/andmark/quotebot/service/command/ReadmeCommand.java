package com.andmark.quotebot.service.command;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.andmark.quotebot.config.BotConfig.*;

@Slf4j
public class ReadmeCommand extends QuoteCommand {

    public ReadmeCommand() {
        super("readme", "Display current version, changelog and private policy");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {} execute /version", user.getId());
        String readmeContent = loadFileContent(readmeFile);
        String changelogContent = loadFileContent(changelogFile);
        String privatePolicyContent = loadFileContent(privatePolicyFile);

        if (!readmeContent.isEmpty() || !changelogContent.isEmpty() || !privatePolicyContent.isEmpty()) {
            List<String> messages = splitContentIntoMessages(
                    changelogContent + "\n\n"
                    + readmeContent + "\n\n"
                    + privatePolicyContent);

            if (!messages.isEmpty()) {
                for (String message : messages) {
                    sendMessage(absSender, chat, message);
                }
            } else {
                sendMessage(absSender, chat, "Содержание Readme в настоящее время недоступно");
            }
        } else {
            sendMessage(absSender, chat, "В настоящее время Readme недоступен. Попробуйте позже.");
        }
    }

    String loadFileContent(String resourcePath) {
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
        StringBuilder currentMessage = new StringBuilder();

        String[] lines = content.split("\n");
        for (String line : lines) {
            if (currentMessage.length() + line.length() + 2 <= maxMessageLength) { // +2 for line break
                if (currentMessage.length() > 0) {
                    currentMessage.append("\n");
                }
                currentMessage.append(line);
            } else {
                messages.add(currentMessage.toString());
                currentMessage.setLength(0);
                currentMessage.append(line);
            }
        }

        if (currentMessage.length() > 0) {
            messages.add(currentMessage.toString());
        }

        return messages;
    }
}