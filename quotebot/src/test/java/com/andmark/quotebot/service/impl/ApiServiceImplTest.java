package com.andmark.quotebot.service.impl;

import com.andmark.quotebot.config.BotConfig;
import com.andmark.quotebot.domain.RequestConfiguration;
import com.andmark.quotebot.domain.enums.UserRole;
import com.andmark.quotebot.dto.*;
import com.andmark.quotebot.service.Bot;
import com.andmark.quotebot.service.keyboard.QuoteKeyboardService;
import com.andmark.quotebot.util.BotAttributes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.andmark.quotebot.config.BotConfig.botToken;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
        "API_BASE_URL=http://localhost:8090/api",
        "adminChatId=629763704",
})
public class ApiServiceImplTest {
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private Bot telegramBot;
    @Mock
    private BotAttributes botAttributes;
    @Mock
    private QuoteKeyboardService quoteKeyboardService;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private MockRestServiceServer mockServer;

    @InjectMocks
    private ApiServiceImpl apiService;

    @Value("${telegram.bot.adminchatid}")
    private Long adminChatId;

    @Test
    public void testGetNextQuoteNoBooksAvailable() {
        QuoteDTO mockQuoteDTO = new QuoteDTO();
        mockQuoteDTO.setContent("No books available. Please scan the catalogue first.");

        ResponseEntity<QuoteDTO> mockResponseEntity = ResponseEntity.ok(mockQuoteDTO);
        when(restTemplate.getForEntity(anyString(), eq(QuoteDTO.class))).thenReturn(mockResponseEntity);

        when(quoteKeyboardService.getEditKeyboardMarkup(anyLong())).thenReturn(null);

        apiService.getNextQuote();

        // Verify that the appropriate message was sent to the admin
        verify(telegramBot).sendMessage(eq(adminChatId), any(), eq(mockQuoteDTO.getContent()));
    }

    @Test
    public void testGetNextQuoteWithValidResponse() {
        QuoteDTO mockQuoteDTO = new QuoteDTO();
        mockQuoteDTO.setId(1L);
        mockQuoteDTO.setContent("Sample quote content");
        mockQuoteDTO.setBookAuthor("author");
        mockQuoteDTO.setBookTitle("title");

        ResponseEntity<QuoteDTO> mockResponseEntity = ResponseEntity.ok(mockQuoteDTO);
        when(restTemplate.getForEntity(anyString(), eq(QuoteDTO.class))).thenReturn(mockResponseEntity);

        when(quoteKeyboardService.getEditKeyboardMarkup(anyLong())).thenReturn(null);

        apiService.getNextQuote();

        // Verify that the appropriate message was sent to the admin
        verify(telegramBot).sendMessage(eq(adminChatId), any(), eq(mockQuoteDTO.getContent()
                + "\n\n" + mockQuoteDTO.getBookAuthor()
                + "\n" + mockQuoteDTO.getBookTitle()));
    }

    @Test
    public void testGetPendingQuotes() {
        // Create mock QuoteDTO objects for the response
        QuoteDTO mockQuote1 = new QuoteDTO();
        mockQuote1.setId(1L);
        mockQuote1.setContent("Quote 1 content");

        QuoteDTO mockQuote2 = new QuoteDTO();
        mockQuote2.setId(2L);
        mockQuote2.setContent("Quote 2 content");

        // Mock the response entity
        ResponseEntity<QuoteDTO[]> mockResponseEntity = ResponseEntity.ok(new QuoteDTO[]{mockQuote1, mockQuote2});
        when(restTemplate.getForEntity(anyString(), eq(QuoteDTO[].class))).thenReturn(mockResponseEntity);

        // Call the method and get the result
        List<QuoteDTO> result = apiService.getPendingQuotes();

        // Verify that restTemplate.getForEntity was called with the correct URL
        verify(restTemplate).getForEntity(eq(BotConfig.API_BASE_URL + "/quotes/get-pending"), eq(QuoteDTO[].class));

        // Verify the result
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId().longValue());
        assertEquals("Quote 1 content", result.get(0).getContent());
        assertEquals(2L, result.get(1).getId().longValue());
        assertEquals("Quote 2 content", result.get(1).getContent());
    }

    @Test
    public void testExistsByUsertgId() {
        long userId = 123L;

        // Mock the response entity
        ResponseEntity<Boolean> mockResponseEntity = ResponseEntity.ok(true);
        when(restTemplate.getForEntity(anyString(), eq(Boolean.class))).thenReturn(mockResponseEntity);

        // Call the method and get the result
        boolean result = apiService.existsByUsertgId(userId);

        // Verify that restTemplate.getForEntity was called with the correct URL
        verify(restTemplate).getForEntity(eq(BotConfig.API_BASE_URL + "/users/exists/" + userId), eq(Boolean.class));

        // Verify the result
        assertTrue(result);
    }

    @Test
    public void testExistsByUsername() {
        String username = "testUser";

        // Mock the response entity
        ResponseEntity<Boolean> mockResponseEntity = ResponseEntity.ok(true);
        when(restTemplate.getForEntity(anyString(), eq(Boolean.class))).thenReturn(mockResponseEntity);

        // Call the method and get the result
        boolean result = apiService.existsByUsername(username);

        // Verify that restTemplate.getForEntity was called with the correct URL
        verify(restTemplate).getForEntity(eq(BotConfig.API_BASE_URL + "/users/username-taken/" + username), eq(Boolean.class));

        // Verify the result
        assertTrue(result);
    }

    @Test
    public void testGetUserRole() {
        long userId = 123L;
        UserRole mockUserRole = UserRole.USER;

        // Mock the response entity
        ResponseEntity<UserRole> mockResponseEntity = ResponseEntity.ok(mockUserRole);
        when(restTemplate.getForEntity(anyString(), eq(UserRole.class))).thenReturn(mockResponseEntity);

        // Call the method and get the result
        UserRole result = apiService.getUserRole(userId);

        // Verify that restTemplate.getForEntity was called with the correct URL
        verify(restTemplate).getForEntity(eq(BotConfig.API_BASE_URL + "/users/get-role/" + userId), eq(UserRole.class));

        // Verify the result
        assertEquals(mockUserRole, result);
    }

    @Test
    public void testDeleteUser() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        // Mocked input values
        Long mockChatId = 123L;
        Long mockUsertgId = 456L;

        // Set up mock response entity
        ResponseEntity<Void> mockResponseEntity = ResponseEntity.ok().build();

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(mockResponseEntity);

        // Call the method
        apiService.deleteUser(mockChatId, mockUsertgId);

        // Verify that restTemplate.exchange was called with the correct URL, method, and null request entity
        mockServer.verify();

        // Verify that restTemplate.exchange was called with the correct URL, method, and null request entity
        verify(restTemplate).exchange(
                eq(BotConfig.API_BASE_URL + "/users/delete/" + mockUsertgId),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }


    @Test
    public void testGetStats() {
        StatsDTO mockStatsDTO = new StatsDTO();
        mockStatsDTO.setBookCount(100L);
        mockStatsDTO.setPublishedQuotesThisYear(50L);
        mockStatsDTO.setPendingQuotesCount(10L);

        // Mock the response entity
        ResponseEntity<StatsDTO> mockResponseEntity = ResponseEntity.ok(mockStatsDTO);
        when(restTemplate.getForEntity(anyString(), eq(StatsDTO.class))).thenReturn(mockResponseEntity);

        // Call the method and get the result
        StatsDTO result = apiService.getStats();

        // Verify that restTemplate.getForEntity was called with the correct URL
        verify(restTemplate).getForEntity(eq(BotConfig.API_BASE_URL + "/stats"), eq(StatsDTO.class));

        // Verify the result
        assertEquals(mockStatsDTO, result);
    }

    @Test
    public void testGetRandomPublishedQuote() {
        QuoteDTO mockQuoteDTO = new QuoteDTO();
        mockQuoteDTO.setContent("Test quote content");

        // Mock the response entity
        ResponseEntity<QuoteDTO> mockResponseEntity = ResponseEntity.ok(mockQuoteDTO);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(QuoteDTO.class))).thenReturn(mockResponseEntity);

        // Call the method and get the result
        QuoteDTO result = apiService.getRandomPublishedQuote();

        // Verify that restTemplate.exchange was called with the correct URL and method
        verify(restTemplate).exchange(
                eq(BotConfig.API_BASE_URL + "/quotes/random/published"),
                eq(HttpMethod.GET),
                eq(null),
                eq(QuoteDTO.class)
        );

        // Verify the result
        assertEquals(mockQuoteDTO, result);
    }

    @Test
    public void testGetWeekPublishedQuotes() {
        QuoteDTO[] mockQuotesArray = new QuoteDTO[2];
        mockQuotesArray[0] = new QuoteDTO();
        mockQuotesArray[0].setContent("Quote 1 content");
        mockQuotesArray[1] = new QuoteDTO();
        mockQuotesArray[1].setContent("Quote 2 content");

        // Mock the response entity
        ResponseEntity<QuoteDTO[]> mockResponseEntity = ResponseEntity.ok(mockQuotesArray);
        when(restTemplate.getForEntity(anyString(), eq(QuoteDTO[].class))).thenReturn(mockResponseEntity);

        // Call the method and get the result
        List<QuoteDTO> result = apiService.getWeekPublishedQuotes();

        // Verify that restTemplate.getForEntity was called with the correct URL
        verify(restTemplate).getForEntity(eq(BotConfig.API_BASE_URL + "/quotes/week"), eq(QuoteDTO[].class));

        // Verify the result
        assertEquals(Arrays.asList(mockQuotesArray), result);
    }

    @Test
    public void testGetRandomGreeting() {
        String mockGreeting = "Hello, welcome to the bot!";

        // Mock the response entity
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(mockGreeting);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(mockResponseEntity);

        // Call the method and get the result
        String result = apiService.getRandomGreeting();

        // Verify that restTemplate.getForEntity was called with the correct URL
        verify(restTemplate).getForEntity(eq(BotConfig.API_BASE_URL + "/greetings/random"), eq(String.class));

        // Verify the result
        assertEquals(mockGreeting, result);
    }

    @Test
    public void testScanBooksSuccess() {
        String directoryPath = "/path/to/books";

        List<BookDTO> mockScannedBooks = new ArrayList<>();
        mockScannedBooks.add(new BookDTO());
        mockScannedBooks.add(new BookDTO());

        // Mock the response entity
        ResponseEntity<List<BookDTO>> mockResponseEntity = ResponseEntity.ok(mockScannedBooks);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<List<BookDTO>>() {
        })))
                .thenReturn(mockResponseEntity);

        // Call the method and get the result
        String result = apiService.scanBooks(directoryPath);

        // Verify that restTemplate.exchange was called with the correct URL, method, and parameterized type reference
        verify(restTemplate).exchange(
                eq(BotConfig.API_BASE_URL + "/scan-books?directoryPath=" + directoryPath),
                eq(HttpMethod.GET),
                eq(null),
                eq(new ParameterizedTypeReference<List<BookDTO>>() {
                })
        );

        // Verify the result
        assertEquals("Scanned books size:\n2", result);
    }

    @Test
    public void testGetScheduledActionStatus() {
        ScheduledActionStatusDTO mockStatusDTO = new ScheduledActionStatusDTO();
        mockStatusDTO.setLastExecuted(LocalDateTime.now());

        // Mock the response entity
        ResponseEntity<ScheduledActionStatusDTO> mockResponseEntity = ResponseEntity.ok(mockStatusDTO);
        when(restTemplate.getForEntity(anyString(), eq(ScheduledActionStatusDTO.class))).thenReturn(mockResponseEntity);

        // Call the method and get the result
        ScheduledActionStatusDTO result = apiService.getScheduledActionStatus();

        // Verify that restTemplate.getForEntity was called with the correct URL
        verify(restTemplate).getForEntity(eq(BotConfig.API_BASE_URL + "/scheduled/random"), eq(ScheduledActionStatusDTO.class));

        // Verify the result
        assertEquals(mockStatusDTO, result);
    }

    @Test
    public void testUpdateScheduledActionStatus() {
        ScheduledActionStatusDTO mockScheduledActionStatusDTO = new ScheduledActionStatusDTO();
        mockScheduledActionStatusDTO.setLastExecuted(LocalDateTime.now());

        // Mock the response entity
        ResponseEntity<Void> mockResponseEntity = ResponseEntity.ok().build();
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(mockResponseEntity);

        // Call the method
        apiService.updateScheduledActionStatus(mockScheduledActionStatusDTO);

        // Verify that restTemplate.exchange was called with the correct URL, method, and request entity
        verify(restTemplate).exchange(
                eq(BotConfig.API_BASE_URL + "/scheduled/update"),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    // Verify that the request entity contains the correct body and headers
                    HttpEntity<?> httpEntity = (HttpEntity<?>) entity;
                    return httpEntity.getBody().equals(mockScheduledActionStatusDTO) &&
                            httpEntity.getHeaders().get("Authorization") != null;
                }),
                eq(Void.class)
        );

    }

    @Test
    public void testProcessPageAndLineNumber() {
        Long userId = 123L;
        int pageNumber = 2;
        int lineNumber = 15;

        PageLineRequestDTO mockRequestDTO = new PageLineRequestDTO(pageNumber, lineNumber);

        ExtractedLinesDTO mockExtractedLinesDTO = new ExtractedLinesDTO();
        mockExtractedLinesDTO.setLines(Collections.singletonList("Line 15 content"));

        // Mock the response entity
        ResponseEntity<ExtractedLinesDTO> mockResponseEntity = ResponseEntity.ok(mockExtractedLinesDTO);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(ExtractedLinesDTO.class)))
                .thenReturn(mockResponseEntity);

        // Call the method and get the result
        ExtractedLinesDTO result = apiService.processPageAndLineNumber(userId, pageNumber, lineNumber);


        // Verify that restTemplate.exchange was called with the correct URL, method, and request entity
        verify(restTemplate).exchange(
                eq(BotConfig.API_BASE_URL + "/books/process-page-and-line"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(ExtractedLinesDTO.class)
        );

        // Verify the result
        assertEquals(mockExtractedLinesDTO, result);
    }

}