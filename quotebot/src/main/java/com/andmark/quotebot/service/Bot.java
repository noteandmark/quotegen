package com.andmark.quotebot.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface Bot {
    void sendMessage(String adminChatId, InlineKeyboardMarkup editKeyboardMarkup, String s);

    void removeKeyboard(String adminChatId);

    void sendImageToChat(String groupChatId, String confirmedUrl);

    void sendImageAttachment(String chatId, byte[] imageBytes, int i);
}
