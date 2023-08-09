package com.andmark.quotebot.service.command;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.dto.QuoteDTO;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

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
        log.info("get quote with id: {}",quoteDTO.getId());

        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        if (quoteDTO != null) {
            message.setText("Here's your quote:\n" + quoteDTO.getContent());
            try {
                absSender.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            message.setText("Sorry, no quotes are available at the moment.");
            try {
                absSender.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
