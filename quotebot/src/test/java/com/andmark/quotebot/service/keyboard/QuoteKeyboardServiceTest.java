package com.andmark.quotebot.service.keyboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class QuoteKeyboardServiceTest {

    @InjectMocks
    private QuoteKeyboardService quoteKeyboardService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateInlineKeyboard() {
        // Given
        List<InlineButton> buttons = List.of(
                new InlineButton("Button 1", "callback1"),
                new InlineButton("Button 2", "callback2")
        );

        // When
        InlineKeyboardMarkup keyboardMarkup = quoteKeyboardService.createInlineKeyboard(buttons);

        // Then
        assertNotNull(keyboardMarkup);
        assertEquals(1, keyboardMarkup.getKeyboard().size());
        assertEquals(2, keyboardMarkup.getKeyboard().get(0).size());
        assertEquals("Button 1", keyboardMarkup.getKeyboard().get(0).get(0).getText());
        assertEquals("callback1", keyboardMarkup.getKeyboard().get(0).get(0).getCallbackData());
        assertEquals("Button 2", keyboardMarkup.getKeyboard().get(0).get(1).getText());
        assertEquals("callback2", keyboardMarkup.getKeyboard().get(0).get(1).getCallbackData());
    }

    @Test
    public void testGetEditKeyboardMarkup() {
        // Given
        Long quoteId = 123L;
        InlineButton editButton = new InlineButton("Edit", "edit-" + quoteId);
        InlineButton acceptButton = new InlineButton("Accept", "confirm-" + quoteId);
        InlineButton rejectButton = new InlineButton("Reject", "reject-" + quoteId);

        // When
        InlineKeyboardMarkup keyboardMarkup = quoteKeyboardService.getEditKeyboardMarkup(quoteId);

        // Then
        assertNotNull(keyboardMarkup);
        assertEquals(1, keyboardMarkup.getKeyboard().size());
        assertEquals(3, keyboardMarkup.getKeyboard().get(0).size());
        assertEquals(editButton.getName(), keyboardMarkup.getKeyboard().get(0).get(0).getText());
        assertEquals(editButton.getCallbackData(), keyboardMarkup.getKeyboard().get(0).get(0).getCallbackData());
        assertEquals(acceptButton.getName(), keyboardMarkup.getKeyboard().get(0).get(1).getText());
        assertEquals(acceptButton.getCallbackData(), keyboardMarkup.getKeyboard().get(0).get(1).getCallbackData());
        assertEquals(rejectButton.getName(), keyboardMarkup.getKeyboard().get(0).get(2).getText());
        assertEquals(rejectButton.getCallbackData(), keyboardMarkup.getKeyboard().get(0).get(2).getCallbackData());
    }
}