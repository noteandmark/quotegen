package com.andmark.quotebot.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface Bot {
    void sendMessage(Long adminChatId, InlineKeyboardMarkup editKeyboardMarkup, String text);

    void removeKeyboard(Long adminChatId);

    void sendImageToChat(Long groupChatId, String confirmedUrl);

    void sendImageAttachment(Long chatId, byte[] imageBytes, int i);
}
