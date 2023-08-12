package com.andmark.quotebot.service.command;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.service.keyboard.InlineButton;
import com.andmark.quotebot.service.keyboard.InlineKeyboardService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
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
public class RequestQuoteCommand extends BotCommand {

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
        log.debug("quoteUrlGetNext = " + quoteUrlGetNext);
        ResponseEntity<QuoteDTO> response = restTemplate.getForEntity(quoteUrlGetNext, QuoteDTO.class);

        QuoteDTO quoteDTO = response.getBody();
        log.info("get quote with id: {}", quoteDTO.getId());

        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message.setText(quoteDTO.getContent());

        // Create an inline keyboard with accept and reject options
        List<InlineButton> buttons = new ArrayList<>();
        buttons.add(new InlineButton("Edit", "edit-" + quoteDTO.getId()));
        buttons.add(new InlineButton("Accept", "confirm-" + quoteDTO.getId()));
        buttons.add(new InlineButton("Reject", "reject-" + quoteDTO.getId()));

        InlineKeyboardMarkup keyboardMarkup = inlineKeyboardService.createInlineKeyboard(buttons);
        message.setReplyMarkup(keyboardMarkup);

        try {
            log.debug("try execute absSender");
            absSender.execute(message);
        } catch (TelegramApiException e) {
            log.error("error in RequestQuoteCommand e: {}", e.getMessage());
        }
    }
}
