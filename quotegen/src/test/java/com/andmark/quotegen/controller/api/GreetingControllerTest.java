package com.andmark.quotegen.controller.api;

import com.andmark.quotegen.controller.api.GreetingController;
import com.andmark.quotegen.service.GreetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class GreetingControllerTest {

    @InjectMocks
    private GreetingController greetingController;
    @Mock
    private GreetingService greetingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRandomGreeting() {
        String expectedGreeting = "some greeting";
        when(greetingService.getRandomGreeting()).thenReturn(expectedGreeting);
        ResponseEntity<String> responseEntity = greetingController.getRandomGreeting();
        assertEquals(ResponseEntity.ok(expectedGreeting), responseEntity);
    }

}