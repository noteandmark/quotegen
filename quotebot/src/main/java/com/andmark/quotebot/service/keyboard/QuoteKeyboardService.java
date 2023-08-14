package com.andmark.quotebot.service.keyboard;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuoteKeyboardService {

    public InlineKeyboardMarkup createInlineKeyboard(List<InlineButton> buttons) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        for (InlineButton button : buttons) {
            InlineKeyboardButton inlineButton = new InlineKeyboardButton(button.getName());
            inlineButton.setCallbackData(button.getCallbackData());
            row.add(inlineButton);
        }
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getEditKeyboardMarkup(String quoteId) {
        List<InlineButton> buttons = new ArrayList<>();
        buttons.add(new InlineButton("Edit", "edit-" + quoteId));
        buttons.add(new InlineButton("Accept", "confirm-" + quoteId));
        buttons.add(new InlineButton("Reject", "reject-" + quoteId));

        InlineKeyboardMarkup keyboardMarkup = createInlineKeyboard(buttons);
        return keyboardMarkup;
    }
}

