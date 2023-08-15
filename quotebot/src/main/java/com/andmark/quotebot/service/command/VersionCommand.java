package com.andmark.quotebot.service.command;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class VersionCommand extends QuoteCommand {
    private final String changelogFilePath = "changelog.json"; // Path to changelog file

    public VersionCommand() {
        super("version", "Display current version and changelog");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        String changelog = loadChangelog();

        sendMessage(absSender, chat, "Эта возможность запланирована в следующих версиях программы");
    }

    private String loadChangelog() {
        // Return a formatted string containing version history and changelogs
        return "";
    }
}