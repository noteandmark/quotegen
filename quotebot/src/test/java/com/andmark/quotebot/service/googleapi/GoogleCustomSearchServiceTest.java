package com.andmark.quotebot.service.googleapi;

import com.andmark.quotebot.dto.GoogleCustomSearchResponse;
import com.andmark.quotebot.util.BotAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class GoogleCustomSearchServiceTest {
    @Mock
    private RestTemplate mockRestTemplate;
    @Mock private BotAttributes mockBotAttributes;
    private GoogleCustomSearchService googleCustomSearchService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        googleCustomSearchService = new GoogleCustomSearchService(mockRestTemplate, mockBotAttributes);
    }

    @Test
    public void testSearchImagesByKeywords() {
        GoogleCustomSearchResponse mockResponse = new GoogleCustomSearchResponse();
        GoogleCustomSearchResponse.Item mockItem = new GoogleCustomSearchResponse.Item();
        GoogleCustomSearchResponse.Image mockImage = new GoogleCustomSearchResponse.Image();
        mockImage.setThumbnailLink("https://example.com/image.jpg");
        mockItem.setImage(mockImage);
        mockResponse.setItems(Collections.singletonList(mockItem));

        when(mockRestTemplate.getForEntity(anyString(), eq(GoogleCustomSearchResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        List<String> imageUrls = googleCustomSearchService.searchImagesByKeywords("cats");

        assertEquals(1, imageUrls.size());
        assertEquals("https://example.com/image.jpg", imageUrls.get(0));
    }

    @Test
    public void testSearchImagesByKeywordsWithNoItems() {
        GoogleCustomSearchResponse mockResponse = new GoogleCustomSearchResponse();

        when(mockRestTemplate.getForEntity(anyString(), eq(GoogleCustomSearchResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        List<String> imageUrls = googleCustomSearchService.searchImagesByKeywords("dogs");

        assertTrue(imageUrls.isEmpty());
    }
}