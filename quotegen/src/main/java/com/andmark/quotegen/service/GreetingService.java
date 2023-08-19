package com.andmark.quotegen.service;

import com.andmark.quotegen.dto.GreetingDTO;

public interface GreetingService extends AbstractService<GreetingDTO>{
    String getRandomGreeting();
}
