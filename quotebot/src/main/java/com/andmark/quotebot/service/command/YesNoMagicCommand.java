package com.andmark.quotebot.service.command;

import com.andmark.quotebot.service.ApiService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class YesNoMagicCommand extends QuoteCommand{
    private final ApiService apiService;
    public YesNoMagicCommand(ApiService apiService) {
        super("da_net", "Ask a question and get an answer in the form of a gif");
        this.apiService = apiService;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        log.debug("user with id = {} execute /da_net", user.getId());
        // Send a question to the user
        SendMessage questionMessage = new SendMessage();
        questionMessage.setChatId(chat.getId());
        questionMessage.setText("Загадайте вопрос в письменном виде или задайте его про себя.....\nОтвет через 10 секунд...");
        try {
            absSender.execute(questionMessage);
        } catch (TelegramApiException e) {
            log.error("Error while sending question message: {}", e.getMessage());
        }
        // Delay for 10 seconds
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            log.error("Error during delay: {}", e.getMessage());
        }

        // Make a request to the yesno.wtf API
        String apiUrl = "https://yesno.wtf/api";

        String imageUrl = apiService.getResponseYesOrNo(apiUrl);
        if (imageUrl != null) {
            log.debug("get answer to user {} with imageUrl = {}", user.getId(), imageUrl);

        } else {
            log.warn("imageUrl in YesNoMagicCommand is null");
            sendMessage(absSender,chat, "Возникла ошибка. Попробуйте повторить позже.");
        }
        sendImage(absSender, chat, imageUrl);
    }

    private void sendImage(AbsSender absSender, Chat chat, String imageUrl) {
        SendAnimation sendAnimation = new SendAnimation();
        sendAnimation.setChatId(chat.getId());
        sendAnimation.setAnimation(new InputFile(imageUrl));

        try {
            log.debug("send animation");
            absSender.execute(sendAnimation);
        } catch (TelegramApiException e) {
            log.error("Error sending image: {}", e.getMessage());
        }
    }
}
