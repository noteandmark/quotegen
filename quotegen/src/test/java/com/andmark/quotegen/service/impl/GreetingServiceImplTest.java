package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.domain.Greeting;
import com.andmark.quotegen.dto.GreetingDTO;
import com.andmark.quotegen.repository.GreetingRepository;
import com.andmark.quotegen.util.MapperConvert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GreetingServiceImplTest {
    @Mock
    private GreetingRepository greetingRepository;
    @Mock
    private MapperConvert<Greeting, GreetingDTO> mapper;
    @InjectMocks
    private GreetingServiceImpl greetingService;

    private GreetingDTO greetingDTO;
    private Greeting greeting;
    private Long greetingId;

    @BeforeEach
    void setUp() {
        greetingId = 1L;
        greetingDTO = new GreetingDTO();
        greeting = new Greeting();
        greeting.setId(greetingId);
        lenient().when(mapper.convertToDTO(greeting, GreetingDTO.class)).thenReturn(greetingDTO);
        lenient().when(mapper.convertToEntity(greetingDTO, Greeting.class)).thenReturn(greeting);
    }

    @Test
    void testSave() {
        // Mocking behavior
        when(greetingService.convertToEntity(greetingDTO)).thenReturn(greeting);
        // Test
        greetingService.save(greetingDTO);
        // Verification
        verify(greetingRepository).save(greeting);
    }

    @Test
    void testFindOne() {
        when(greetingRepository.findById(greetingId)).thenReturn(Optional.of(greeting));
        GreetingDTO result = greetingService.findOne(greetingId);
        assertEquals(greetingDTO, result);
    }

    @Test
    void testFindAll() {
        List<Greeting> all = new ArrayList<>();
        all.add(greeting);

        when(greetingRepository.findAll()).thenReturn(all);
        greetingService.findAll();
        verify(greetingRepository, only()).findAll();
    }

    @Test
    void testUpdateGreeting() {
        // Mocking
        GreetingDTO updatedGreetingDTO = new GreetingDTO();
        updatedGreetingDTO.setMessage("new greeting message");
        Greeting updatedGreeting = new Greeting();
        updatedGreeting.setMessage(updatedGreetingDTO.getMessage());

        // Mocking behavior
        when(greetingService.convertToEntity(updatedGreetingDTO)).thenReturn(updatedGreeting);
        // Test
        greetingService.update(updatedGreetingDTO);
        // Verification
        verify(greetingRepository).save(updatedGreeting);
    }

    @Test
    void testDelete() {
        // Test
        greetingService.delete(greetingId);
        // Verification
        verify(greetingRepository).deleteById(greetingId);
    }

    @Test
    void testGetRandomGreeting() {
        List<Greeting> mockGreetingList = new ArrayList<>();
        mockGreetingList.add(new Greeting());
        when(greetingRepository.findAll()).thenReturn(mockGreetingList);

        greetingService.getRandomGreeting();

        verify(greetingRepository).findAll();
    }
}