package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.domain.Greeting;
import com.andmark.quotegen.dto.GreetingDTO;
import com.andmark.quotegen.repository.GreetingRepository;
import com.andmark.quotegen.service.GreetingService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class GreetingServiceImpl implements GreetingService {
    private final GreetingRepository greetingRepository;

    public GreetingServiceImpl(GreetingRepository greetingRepository) {
        this.greetingRepository = greetingRepository;
    }

    @Override
    public void save(GreetingDTO greetingDTO) {

    }

    @Override
    public GreetingDTO findOne(Long id) {
        return null;
    }

    @Override
    public List<GreetingDTO> findAll() {
        return null;
    }

    @Override
    public void update(Long id, GreetingDTO greetingDTO) {

    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public String getRandomGreeting() {
        List<Greeting> greetings = greetingRepository.findAll();
        if (!greetings.isEmpty()) {
            int randomIndex = new Random().nextInt(greetings.size());
            return greetings.get(randomIndex).getMessage();
        }
        return "";
    }
}
