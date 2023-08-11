package com.andmark.quotebot.service.command;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.service.QuoteBot;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RequestQuoteCommand extends BotCommand {

    public RequestQuoteCommand() {
        super("requestquote", "Request a new quote");
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
        log.debug("getting quoteDTO: {}, in chatId = {}", quoteDTO, chat.getId());
        message.setText(quoteDTO.getContent());
        log.debug("creating inline keyboard");

        // Create an inline keyboard with accept and reject options
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton editButton = new InlineKeyboardButton("Edit");
        editButton.setCallbackData("edit-" + quoteDTO.getId());
        row.add(editButton);

        InlineKeyboardButton acceptButton = new InlineKeyboardButton("Accept");
        acceptButton.setCallbackData("confirm-" + quoteDTO.getId());
        row.add(acceptButton);

        InlineKeyboardButton rejectButton = new InlineKeyboardButton("Reject");
        rejectButton.setCallbackData("reject-" + quoteDTO.getId());
        row.add(rejectButton);

        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            log.debug("try execute absSender");
            absSender.execute(message);
        } catch (TelegramApiException e) {
            log.error("error in RequestQuoteCommand e: {}", e.getMessage());
        }
    }
}
