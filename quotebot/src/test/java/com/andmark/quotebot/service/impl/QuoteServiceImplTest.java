package com.andmark.quotebot.service.impl;

import com.andmark.quotebot.domain.RequestConfiguration;
import com.andmark.quotebot.domain.enums.QuoteStatus;
import com.andmark.quotebot.dto.QuoteDTO;
import com.andmark.quotebot.dto.UserDTO;
import com.andmark.quotebot.exception.QuoteException;
import com.andmark.quotebot.service.ApiService;
import com.andmark.quotebot.service.Bot;
import com.andmark.quotebot.service.UserService;
import com.andmark.quotebot.service.enums.BotState;
import com.andmark.quotebot.service.googleapi.GoogleCustomSearchService;
import com.andmark.quotebot.service.keyboard.QuoteKeyboardService;
import com.andmark.quotebot.util.BotAttributes;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.telegram.telegrambots.meta.api.objects.*;

import static com.andmark.quotebot.config.BotConfig.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

@SpringBootTest
@TestPropertySource(properties = {
        "API_BASE_URL=http://localhost:8090/api",
        "adminChatId=629763704",
})
public class QuoteServiceImplTest {
    @Mock
    private Bot telegramBot;
    @Mock
    private BotAttributes botAttributes;
    @Mock
    private ApiService apiService;
    @Mock
    private UserService userService;
    @Mock
    private GoogleCustomSearchService googleCustomSearchService;
    @Mock
    private QuoteKeyboardService quoteKeyboardService;
    @Mock
    private UserRegistrationService userRegistrationService;

    @InjectMocks
    private QuoteServiceImpl quoteService;

    @Test
    public void testHandleIncomingMessage_ResetCommand() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Mocking the static method getUserCurrentBotState
            mockedBotAttributes.when(() -> BotAttributes.getUserCurrentBotState(anyLong()))
                    .thenReturn(BotState.START);

            // Mocked input values
            Update update = createUpdateWithMessageText("сброс");

            // Call the method
            quoteService.handleIncomingMessage(update);

            // Verify that the appropriate static method is called
            mockedBotAttributes.verify(() -> BotAttributes.getUserCurrentBotState(anyLong()));
            verify(telegramBot).sendMessage(anyLong(), any(), eq("Состояние сброшено"));
        }
    }

    @Test
    public void testHandleIncomingMessage_AwaitingImageChoice() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Mocking the static method getUserCurrentBotState
            mockedBotAttributes.when(() -> BotAttributes.getUserCurrentBotState(anyLong()))
                    .thenReturn(BotState.AWAITING_IMAGE_CHOICE);

            // Set up input
            Update update = new Update();
            Message message = new Message();
            Chat chat = new Chat();
            chat.setId(123L);
            message.setChat(chat);
            message.setMessageId(456);
            message.setText("5");
            User user = new User();
            user.setId(789L);
            message.setFrom(user);
            update.setMessage(message);

            // Mocking methods
            when(botAttributes.getImageUrls()).thenReturn(List.of("image1.jpg", "image2.jpg"));

            // Call the method to test
            quoteService.handleIncomingMessage(update);

            // Verify interactions
            verify(telegramBot).sendMessage(eq(123L), isNull(), eq("Выбери изображение от 0 до 10 (0 - пост без изображений)."));
//            mockedBotAttributes.verify(() -> BotAttributes.setUserCurrentBotState(anyLong(), eq(BotState.AWAITING_PUBLISHING)));
        }
    }

    @Test
    public void testHandleCallbackQuery_EditAction() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Mocking the static method getUserCurrentBotState
            mockedBotAttributes.when(() -> BotAttributes.getUserCurrentBotState(anyLong()))
                    .thenReturn(BotState.START);
            // Mocked input values
            CallbackQuery callbackQuery = mock(CallbackQuery.class);
            Message message = mock(Message.class);
            when(callbackQuery.getMessage()).thenReturn(message); // Return the mocked Message object

            // Set up User object
            User user = mock(User.class);
            when(callbackQuery.getFrom()).thenReturn(user);

            // Set up other properties of the callbackQuery, message, and other objects
            when(callbackQuery.getData()).thenReturn("edit-123"); // Set the data
            when(callbackQuery.getMessage().getChatId()).thenReturn(123L); // Set the chatId
            Update update = new Update();
            update.setCallbackQuery(callbackQuery);

            // Mocking methods
            when(botAttributes.getLastMessageId()).thenReturn(456);
            when(botAttributes.getLastCallbackMessage()).thenReturn("write quoteText starting with q:");
            when(quoteKeyboardService.getEditKeyboardMarkup(anyLong())).thenReturn(null);

            // Call the method to test
            quoteService.handleCallbackQuery(update);

            // Verify interactions
            verify(telegramBot).removeKeyboard(eq(123L));
            verify(telegramBot).sendMessage(eq(123L), isNull(), eq("write quoteText starting with q:"));
        }
    }

    @Test
    public void testHandleActionForBotState_ConfirmAction() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Mocking the static method getUserCurrentBotState
            mockedBotAttributes.when(() -> BotAttributes.getUserCurrentBotState(anyLong()))
                    .thenReturn(BotState.START);
            // Set up mock values
            String callbackData = "confirm-123";
            Long chatId = 789L;
            Long userId = 456L;
            Long quoteId = 123L;
            Update update = createUpdateWithCallbackData(callbackData, chatId, userId);

            when(botAttributes.getLastCallbackMessage()).thenReturn("some content");

            // Call handleCallbackQuery method
            quoteService.handleCallbackQuery(update);

            // Verify interactions
            verify(botAttributes).getLastCallbackMessage();

            mockedBotAttributes.verify(() -> BotAttributes.getUserCurrentBotState(anyLong()));
            verify(botAttributes).getLastCallbackMessage();
            verify(telegramBot).removeKeyboard(chatId); // You can add any necessary verifications for telegramBot interactions
            verify(telegramBot).sendMessage(chatId, null, "Выбери изображение, введя цифру в пределах [1-" + botAttributes.getImageUrls().size() + "]"
                    + "\n" + "Или введи [0] для поста без картинки");
            mockedBotAttributes.verify(() -> BotAttributes.setUserCurrentBotState(adminChatId, BotState.AWAITING_IMAGE_CHOICE));
        }
    }

    @Test
    public void testHandleActionForBotState_RejectAction() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Mocking the static method getUserCurrentBotState
            mockedBotAttributes.when(() -> BotAttributes.getUserCurrentBotState(anyLong()))
                    .thenReturn(BotState.START);

            // Create a mock CallbackQuery
            CallbackQuery callbackQuery = mock(CallbackQuery.class);
            Message message = mock(Message.class);

            when(callbackQuery.getMessage()).thenReturn(message);
            when(callbackQuery.getData()).thenReturn("reject-123"); // Set the data
            when(callbackQuery.getMessage().getChatId()).thenReturn(123L); // Set the chatId

            // Create a mock Update
            Update update = new Update();
            update.setCallbackQuery(callbackQuery);
            User user = mock(User.class);
            when(callbackQuery.getFrom()).thenReturn(user);

            // Call the method to test
            quoteService.handleCallbackQuery(update);

            // Verify interactions
            verify(apiService).sendRequestAndHandleResponse(any(RequestConfiguration.class));
        }
    }

    @Test
    public void testConfirmQuote_SendImagesToChoice() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Mocking the static method getUserCurrentBotState
            mockedBotAttributes.when(() -> BotAttributes.getUserCurrentBotState(anyLong()))
                    .thenReturn(BotState.AWAITING_IMAGE_CHOICE);

            // Set up mock values
            String content = "Test content";
            Long chatId = 123L;
            Long quoteId = 456L;
            List<String> imageUrls = List.of("image1.jpg", "image2.jpg");

            // Mock quoteText and botAttributes
            Stack<String> quoteTextStack = new Stack<>();
            quoteTextStack.push(content);
            when(botAttributes.getLastMessageId()).thenReturn(456);
            when(botAttributes.getLastCallbackMessage()).thenReturn("Test content");
            when(botAttributes.getImageUrls()).thenReturn(imageUrls);

            // Mock GoogleCustomSearchService
            when(googleCustomSearchService.searchImagesByKeywords(anyString())).thenReturn(imageUrls);

            // Create a QuoteDTO instance for confirmation
            QuoteDTO quoteDTO = new QuoteDTO();
            quoteDTO.setId(quoteId);
            quoteDTO.setContent(content);
            quoteDTO.setImageUrl(imageUrls.get(0));

            // Mock telegramBot
            doNothing().when(telegramBot).removeKeyboard(eq(chatId));
            doNothing().when(telegramBot).sendMessage(eq(chatId), isNull(), anyString());

            // Mock QuoteService and set up return value for downloadImage method
            QuoteServiceImpl spyQuoteService = spy(quoteService);
            doReturn(new byte[0]).when(apiService).downloadImage(anyString());

            // Call confirmQuote method
            spyQuoteService.confirmQuote(chatId, quoteId);

            // Verify interactions
            verify(botAttributes).getLastCallbackMessage();
            verify(botAttributes).setConfirmedContent(content);
            verify(botAttributes).setQuoteId(quoteId);
            verify(botAttributes).setImageUrls(eq(imageUrls));

            // Verify sendImagesToChoice method
            verify(telegramBot).removeKeyboard(eq(chatId));
            verify(telegramBot).sendMessage(eq(chatId), isNull(), startsWith("Выбери изображение, введя цифру в пределах"));
            mockedBotAttributes.verify(() -> BotAttributes.setUserCurrentBotState(adminChatId, BotState.AWAITING_IMAGE_CHOICE));
            for (int i = 0; i < imageUrls.size(); i++) {
                verify(telegramBot).sendImageAttachment(eq(chatId), any(byte[].class), eq(i + 1));
            }
        }
    }

    @Test
    public void testPublishQuoteToGroup_WithContent() {
        // Set up mock values
//        Long groupId = -849307184L; // test case: here is needed your testing telegram chat id
        Long groupId = groupChatId; // prod package case

        String content = "Test content";
//        String botUsername = "@test_quote_bot"; // here is your testing bot name
        String botUsername = "@rimay_bot"; // here is your prod bot name
        String randomGreeting = "Hello!";

        // Mock apiService
        when(apiService.getRandomGreeting()).thenReturn(randomGreeting);

        // Call publishQuoteToGroup method
        QuoteDTO quoteDTO = new QuoteDTO();
        quoteDTO.setContent(content);
        quoteDTO.setImageUrl(null); // No image for this test
        QuoteDTO result = quoteService.publishQuoteToGroup(quoteDTO);

        // Verify interactions
        verify(apiService).getRandomGreeting();
        verify(telegramBot).sendMessage(eq(groupId), isNull(), eq(randomGreeting + "\nВаш бот Книголюб, " + botUsername));
        verify(telegramBot).sendMessage(eq(groupId), isNull(), eq(content));

        // Verify quoteDTO status
        assertEquals(QuoteStatus.PUBLISHED, result.getStatus());
    }

    @Test
    public void testPublishQuoteToGroup_WithImage() {
        // Set up mock values
//        Long groupId = -849307184L; // test case: here is needed your testing telegram chat id
        Long groupId = groupChatId; // test case: here is needed your testing telegram chat id

        String imageUrl = "image.jpg";

        // Call publishQuoteToGroup method
        QuoteDTO quoteDTO = new QuoteDTO();
        quoteDTO.setContent("some context");
        quoteDTO.setImageUrl(imageUrl);

        when(apiService.getRandomGreeting()).thenReturn("Some greeting");

        QuoteDTO result = quoteService.publishQuoteToGroup(quoteDTO);

        // Verify interactions
        verify(telegramBot).sendImageToChat(eq(groupId), eq(imageUrl));

        // Verify quoteDTO status
        assertEquals(QuoteStatus.PUBLISHED, result.getStatus());
    }

    @Test
    public void testSendQuoteSavedToDatabase() {
        // Set up mock values
        String confirmUrl = API_BASE_URL + "/quotes/confirm";
        QuoteDTO quoteDTO = new QuoteDTO();
        String message = "Quote saved successfully";

        RequestConfiguration expectedRequestConfig = new RequestConfiguration.Builder()
                .url(confirmUrl)
                .httpMethod(HttpMethod.POST)
                .requestBody(quoteDTO)
                .chatId(adminChatId)
                .successMessage(message)
                .keyboard(null)
                .build();

        // Call sendQuoteSavedToDatabase method
        quoteService.sendQuoteSavedTODatabase(quoteDTO, message);

        // Normalize line breaks in both expected and actual results
        String expectedNormalized = normalizeLineBreaks(expectedRequestConfig.toString());
        String actualNormalized = normalizeLineBreaks(getCapturedRequestConfig().toString());

        // Verify interactions
        assertEquals(expectedNormalized, actualNormalized);
    }

    @Test
    public void testHandleImageChoiceResponse_ValidChoice() {
        // Set up mock values
        Long chatId = 123456789L;
        String userInput = "2"; // Valid choice
        String messageText = "Публиковать [сразу] или [отложить]?";

        // Set up botAttributes
        botAttributes.setImageUrls(Arrays.asList("image1.jpg", "image2.jpg", "image3.jpg"));
        when(botAttributes.getLastCallbackMessage()).thenReturn(messageText);
        when(botAttributes.getImageUrls()).thenReturn(Arrays.asList("image1.jpg", "image2.jpg", "image3.jpg"));

        // Call handleImageChoiceResponse method
        quoteService.handleImageChoiceResponse(chatId, userInput);

        // Verify interactions
        verify(telegramBot).sendMessage(eq(chatId), any(), eq(messageText));
        verify(botAttributes).setConfirmedUrl(eq("image2.jpg"));
        verify(telegramBot, times(1)).sendMessage(eq(chatId), any(), anyString());
    }

    @Test
    public void testHandleImageChoiceResponse_InvalidChoice() {
        // Set up mock values
        Long chatId = 123456789L;
        String userInput = "15"; // Invalid choice

        // Set up botAttributes
        BotAttributes.setUserCurrentBotState(adminChatId, BotState.AWAITING_IMAGE_CHOICE);
        botAttributes.setImageUrls(Arrays.asList("image1.jpg", "image2.jpg", "image3.jpg"));

        // Call handleImageChoiceResponse method
        quoteService.handleImageChoiceResponse(chatId, userInput);

        // Verify interactions
        verify(telegramBot, never()).removeKeyboard(chatId);
        verify(botAttributes, never()).setConfirmedUrl(anyString());
        verify(telegramBot).sendMessage(eq(chatId), any(), eq("Выбери изображение от 0 до 10 (0 - пост без изображений)."));
        verify(botAttributes, never()).setConfirmedUrl(null);
        verify(telegramBot, times(1)).sendMessage(eq(chatId), any(), anyString());
    }

    @Test
    public void testPostingQuote_ImmediatePublish() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Mocking the static method getUserCurrentBotState
            mockedBotAttributes.when(() -> BotAttributes.getUserCurrentBotState(anyLong()))
                    .thenReturn(BotState.AWAITING_PUBLISHING);
            // Set up mock values
            Long chatId = 123456789L;
            Long quoteId = 987654321L;
            String content = "Test quote content";
            String confirmedImageUrl = "image.jpg";
            String messageText = "Публиковать [сразу] или [отложить]?";

            // Set up botAttributes
            botAttributes.setQuoteId(quoteId);
            botAttributes.setConfirmedContent(content);
            botAttributes.setConfirmedUrl(confirmedImageUrl);
            // When
            when(botAttributes.getLastCallbackMessage()).thenReturn(messageText);

            // Call postingQuote method
            quoteService.postingQuote(chatId);

            // Verify interactions
            verify(telegramBot).sendMessage(eq(chatId), any(), eq(messageText));
            verify(telegramBot, times(1)).sendMessage(eq(chatId), any(), anyString());
        }
    }

    @Test
    public void testPostingQuote_Postpone() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Mocking the static method getUserCurrentBotState
            mockedBotAttributes.when(() -> BotAttributes.getUserCurrentBotState(anyLong()))
                    .thenReturn(BotState.START);
            // Set up mock values
            Long chatId = 123456789L;
            Long quoteId = 987654321L;
            String content = "Test quote content";
            LocalDateTime pendingTime = LocalDateTime.now();

            // Set up botAttributes
            botAttributes.setQuoteId(quoteId);
            botAttributes.setConfirmedContent(content);

            // Call postponePublishing method
            quoteService.postponePublishing(pendingTime);

            // Verify interactions
            verify(apiService).sendRequestAndHandleResponse(any(RequestConfiguration.class)); // Verify sendRequestAndHandleResponse call
            mockedBotAttributes.verify(() -> BotAttributes.setUserCurrentBotState(adminChatId, BotState.START));
        }
    }

    @Test
    public void testHandlePublishingChoiceResponse_Immediate() {
        // Set up mock values
        Long chatId = 123456789L;
        Long quoteId = 987654321L;
        String userInput = "сразу";

        // Set up botAttributes
        BotAttributes.setUserCurrentBotState(adminChatId, BotState.AWAITING_PUBLISHING);
        botAttributes.setQuoteId(quoteId);
        botAttributes.setConfirmedContent("Test quote content");
        botAttributes.setConfirmedUrl("image.jpg");

        // Call handlePublishingChoiceResponse method
        quoteService.handlePublishingChoiceResponse(chatId, userInput);

        // Verify interactions
        verify(apiService).sendRequestAndHandleResponse(any(RequestConfiguration.class)); // Verify sendRequestAndHandleResponse call
        verify(botAttributes).setConfirmedUrl("image.jpg");
    }

    @Test
    public void testHandlePublishingChoiceResponse_Postpone() {
        // Set up mock values
        Long chatId = 123456789L;
        String userInput = "отложить";

        // Set up botAttributes
        BotAttributes.setUserCurrentBotState(adminChatId, BotState.AWAITING_PUBLISHING);

        // Call handlePublishingChoiceResponse method
        quoteService.handlePublishingChoiceResponse(chatId, userInput);

        // Verify interactions
        verify(telegramBot).sendMessage(eq(chatId), any(), eq("Напиши дату публикации в виде: [yyyy-MM-dd HH:mm:ss] или напиши [случайно]"));
    }

    @Test
    public void testHandlePublishingChoiceResponse_InvalidChoice() {
        // Set up mock values
        Long chatId = 123456789L;
        String userInput = "invalid_choice";

        // Set up botAttributes
        BotAttributes.setUserCurrentBotState(adminChatId, BotState.AWAITING_PUBLISHING);

        // Call handlePublishingChoiceResponse method
        quoteService.handlePublishingChoiceResponse(chatId, userInput);

        // Verify interactions
        verify(telegramBot).sendMessage(eq(chatId), any(), eq("Напиши выбор в виде: [сразу] или [отложить]"));
    }

    @Test
    public void testHandleUsernameInputResponse_UsernameAvailable() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Given
            Update update = createUpdateWithMessageText("newUsername");
            when(userService.isUsernameTaken("newUsername")).thenReturn(false);
            doNothing().when(userRegistrationService).handleUsernameInput(anyLong(), anyLong(), eq("newUsername"));
            // When
            quoteService.handleUsernameInputResponse(update);
            // Then
            verify(userRegistrationService, times(1)).handleUsernameInput(anyLong(), anyLong(), eq("newUsername"));
            verify(telegramBot, never()).sendMessage(anyLong(), any(), anyString());
            mockedBotAttributes.verify(() -> BotAttributes.setUserCurrentBotState(eq(update.getMessage().getFrom().getId()), eq(BotState.AWAITING_PASSWORD_INPUT)));
        }
    }

    @Test
    public void testHandleUsernameInputResponse_UsernameTaken() {
        // Given
        Update update = createUpdateWithMessageText("takenUsername");
        when(userService.isUsernameTaken("takenUsername")).thenReturn(true);

        // When
        quoteService.handleUsernameInputResponse(update);

        // Then
        verify(userRegistrationService, never()).handleUsernameInput(anyLong(), anyLong(), anyString());
        verify(telegramBot, times(1)).sendMessage(anyLong(), any(), eq("Имя пользователя уже занято. Пожалуйста, выберите другое имя."));
    }

    @Test
    public void testHandlePasswordInputResponse_UserDTONotNull() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Given
            Update update = createUpdateWithMessageText("newPassword");
            UserDTO userDTO = new UserDTO(); // You can initialize this as needed
            when(userRegistrationService.handlePasswordInput(anyLong(), anyLong(), eq("newPassword"))).thenReturn(userDTO);

            // When
            quoteService.handlePasswordInputResponse(update);

            // Then
            verify(apiService, times(1)).registerUser(anyLong(), eq(userDTO));
            verify(userRegistrationService, times(1)).completeRegistration(anyLong(), anyLong());
            // Verify that BotAttributes.clear is called for the specific user
            mockedBotAttributes.verify(() -> BotAttributes.clear(anyLong()), times(1));
        }
    }

    @Test
    public void testHandlePasswordInputResponse_UserDTONull() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Given
            Update update = createUpdateWithMessageText("invalidPassword");
            when(userRegistrationService.handlePasswordInput(anyLong(), anyLong(), eq("invalidPassword"))).thenReturn(null);

            // When
            quoteService.handlePasswordInputResponse(update);

            // Then
            verify(apiService, never()).registerUser(anyLong(), any(UserDTO.class));
            verify(userRegistrationService, never()).completeRegistration(anyLong(), anyLong());
            mockedBotAttributes.verify(() -> BotAttributes.clear(anyLong()), never());
        }
    }

    @Test
    public void testHandlePageNumberInput_ValidInput() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Given
            Long userId = 123L;
            String userInput = "42";

            // When
            assertDoesNotThrow(() -> quoteService.handlePageNumberInput(userId, userInput));

            // Then
            verify(telegramBot, times(1)).sendMessage(eq(userId), eq(null), eq("Хорошо! Теперь напиши номер строки."));
            mockedBotAttributes.verify(() -> BotAttributes.setUserCurrentBotState(eq(userId), eq(BotState.AWAITING_LINE_NUMBER)), times(1));
            verify(botAttributes, times(1)).setPageNumber(eq(userId), eq(42));
        }
    }

    @Test
    public void testHandlePageNumberInput_InvalidInput() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Given
            Long userId = 123L;
            String userInput = "invalid";

            // When
            QuoteException exception = assertThrows(QuoteException.class, () -> quoteService.handlePageNumberInput(userId, userInput));

            // Then
            assertEquals("handlePageNumberInput wrong input:java.lang.NumberFormatException: For input string: " +
                            "\"" + userInput + "\"",
                    exception.getMessage());
            verify(telegramBot, times(1)).sendMessage(eq(userId), eq(null), eq("Введите число!"));
            mockedBotAttributes.verify(() -> BotAttributes.setUserCurrentBotState(anyLong(), any(BotState.class)), never());
            verify(botAttributes, never()).setPageNumber(anyLong(), anyInt());
        }
    }

    @Test
    public void testHandleReportInput() {
        try (MockedStatic<BotAttributes> mockedBotAttributes = mockStatic(BotAttributes.class)) {
            // Given
            User user = createUserMock(123L, "testUser");
            String reportMessage = "This is a report message";

            // When
            quoteService.handleReportInput(user, reportMessage);

            // Then
            String expectedReportMessage = "Новое сообщение от пользователя @testUser:\nThis is a report message";
            verify(telegramBot, times(1)).sendMessage(eq(adminChatId), eq(null), eq(expectedReportMessage));
            verify(telegramBot, times(1)).sendMessage(eq(123L), eq(null), eq("Ваше сообщение отправлено."));
            mockedBotAttributes.verify(() -> BotAttributes.clear(eq(123L)), times(1));
        }
    }

    private User createUserMock(long id, String userName) {
        User user = new User();
        user.setId(id);
        user.setUserName(userName);
        return user;
    }

    private Update createUpdateWithMessageText(String text) {
        Chat chat = new Chat();
        chat.setId(123L);

        Message message = new Message();
        message.setChat(chat);
        message.setMessageId(456);
        message.setText(text);

        User user = new User();
        user.setId(789L);
        message.setFrom(user);

        Update update = new Update();
        update.setMessage(message);

        return update;
    }

    private String normalizeLineBreaks(String input) {
        return input.replace("\r\n", "\n").replace("\r", "\n");
    }

    private RequestConfiguration getCapturedRequestConfig() {
        ArgumentCaptor<RequestConfiguration> requestConfigCaptor = ArgumentCaptor.forClass(RequestConfiguration.class);
        verify(apiService).sendRequestAndHandleResponse(requestConfigCaptor.capture());
        return requestConfigCaptor.getValue();
    }

    private Update createUpdateWithCallbackData(String callbackData, Long chatId, Long userId) {
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);
        when(callbackQuery.getMessage()).thenReturn(message); // Return the mocked Message object

        // Set up User object
        User user = mock(User.class);
        when(callbackQuery.getFrom()).thenReturn(user);
        when(callbackQuery.getData()).thenReturn(callbackData); // Set the callback data

        // Set up other properties of the callbackQuery, message, and other objects
        when(callbackQuery.getMessage().getChatId()).thenReturn(chatId); // Set the chatId
        Update update = new Update();
        update.setCallbackQuery(callbackQuery);
        return update;
    }

}