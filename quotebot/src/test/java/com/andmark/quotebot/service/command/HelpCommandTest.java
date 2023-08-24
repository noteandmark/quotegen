package com.andmark.quotebot.service.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HelpCommandTest {
    @Mock
    private AbsSender mockAbsSender;
    @Mock
    private Chat mockChat;

    private HelpCommand helpCommand;

    @BeforeEach
    public void setUp() {
        helpCommand = new HelpCommand();
    }

    @Test
    public void testExecute() throws TelegramApiException {
        User mockUser = new User();
        mockUser.setId(123L); // Set user ID

        helpCommand.execute(mockAbsSender, mockUser, mockChat, new String[0]);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mockAbsSender, times(1)).execute(sendMessageCaptor.capture());

        SendMessage sentMessage = sendMessageCaptor.getValue();
        String expectedText = "Добро пожаловать! Книголюб приветствует!\n\n" +
                "У меня есть такое меню команд:\n" +
                "/start - проверить: в сети ли бот\n" +
                "/help - отобразить справочную информацию\n" +
                "/stats - разная статистика\n" +
                "/da_net - задай вопрос, узнай ответ: да или нет\n" +
                "/divination - гадание на книгах (user)\n" +
                "/getquote - получить случайную цитату из опубликованных (user)\n" +
                "/quotes_for_week - все цитаты за неделю (user)\n" +
                "/version - версия и список изменений\n" +
                "/signup - зарегистрироваться\n" +
                "/signout - сброс логина, пароля\n" +
                "/reset - сброс до настроек по умолчанию\n" +
                "/report - сообщить админу что-либо (баги, предложения, отзывы, др.)\n" +
                "/scanbooks - поиск электронных книг (админ)\n" +
                "/requestquote - сгенерировать цитату (админ)\n";
        assertEquals(expectedText, sentMessage.getText());
    }

}