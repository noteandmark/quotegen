package com.andmark.quotebot.service.command;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.service.keyboard.InlineButton;
import com.andmark.quotebot.service.keyboard.InlineKeyboardService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RequestQuoteCommand extends QuoteCommand {

    private final InlineKeyboardService inlineKeyboardService;

    public RequestQuoteCommand(InlineKeyboardService inlineKeyboardService) {
        super("requestquote", "Request a new quote");
        this.inlineKeyboardService = inlineKeyboardService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("execute RequestQuoteCommand");
        // Make a request to the REST API to get the next quote
        RestTemplate restTemplate = new RestTemplate();
        String quoteUrlGetNext = BotConfig.API_BASE_URL + "/quotes/get-next";
        ResponseEntity<QuoteDTO> response = restTemplate.getForEntity(quoteUrlGetNext, QuoteDTO.class);

        QuoteDTO quoteDTO = response.getBody();
        log.info("get quote with id: {}", quoteDTO.getId());

        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message.setText(formatQuoteText(quoteDTO));

        // Create an inline keyboard with accept and reject
        setKeyboard(message, quoteDTO.getId());
        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            log.error("error in RequestQuoteCommand e: {}", e.getMessage());
        }
    }

    private void setKeyboard(SendMessage message, Long id) {
        List<InlineButton> buttons = createStandardButtons(id);
        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardService.createInlineKeyboard(buttons);
        message.setReplyMarkup(keyboardMarkup);
    }

    private String formatQuoteText(QuoteDTO quoteDTO) {
        return quoteDTO.getContent() + "\n\n"
                + quoteDTO.getBookAuthor() + "\n"
                + quoteDTO.getBookTitle();
    }

    private List<InlineButton> createStandardButtons(Long quoteId) {
        List<InlineButton> buttons = new ArrayList<>();
        buttons.add(new InlineButton("Edit", "edit-" + quoteId));
        buttons.add(new InlineButton("Accept", "confirm-" + quoteId));
        buttons.add(new InlineButton("Reject", "reject-" + quoteId));
        return buttons;
    }

}
