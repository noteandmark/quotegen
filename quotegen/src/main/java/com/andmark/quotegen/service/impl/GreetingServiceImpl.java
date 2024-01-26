package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.domain.Greeting;
import com.andmark.quotegen.dto.GreetingDTO;
import com.andmark.quotegen.repository.GreetingRepository;
import com.andmark.quotegen.service.GreetingService;
import com.andmark.quotegen.util.MapperConvert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GreetingServiceImpl implements GreetingService {
    private final GreetingRepository greetingRepository;
    private final MapperConvert<Greeting, GreetingDTO> mapper;

    public GreetingServiceImpl(GreetingRepository greetingRepository, MapperConvert<Greeting, GreetingDTO> mapper) {
        this.greetingRepository = greetingRepository;
        this.mapper = mapper;
    }

    @Override
    public GreetingDTO save(GreetingDTO greetingDTO) {
        log.debug("saving greeting");
        greetingRepository.save(convertToEntity(greetingDTO));
        log.info("save greeting {}", greetingDTO);
        return greetingDTO;
    }

    @Override
    public GreetingDTO findOne(Long id) {
        log.debug("find greeting by id {}", id);
        Optional<Greeting> foundGreeting = greetingRepository.findById(id);
        log.info("find greeting {}", foundGreeting);
        return foundGreeting.map(this::convertToDTO).orElse(null);
    }

    @Override
    public List<GreetingDTO> findAll() {
        log.debug("find all greetings");
        List<Greeting> greetingList = greetingRepository.findAll();
        log.info("founded greetingList = {}", greetingList);
        return convertToDtoList(greetingList);
    }

    @Override
    public GreetingDTO update(GreetingDTO updatedGreetingDTO) {
        log.debug("update greeting updatedGreetingDTO with id: {}", updatedGreetingDTO.getId());
        Greeting updatedGreeting = convertToEntity(updatedGreetingDTO);
        greetingRepository.save(updatedGreeting);
        log.info("updated greeting");
        return updatedGreetingDTO;
    }

    @Override
    public void delete(Long id) {
        log.debug("delete greeting by id {}", id);
        greetingRepository.deleteById(id);
        log.info("delete greeting with id {} perform", id);
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

    private GreetingDTO convertToDTO(Greeting greeting) {
        return mapper.convertToDTO(greeting, GreetingDTO.class);
    }

    Greeting convertToEntity(GreetingDTO greetingDTO) {
        return mapper.convertToEntity(greetingDTO, Greeting.class);
    }

    private List<GreetingDTO> convertToDtoList(List<Greeting> greetings) {
        return greetings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
