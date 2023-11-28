package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.domain.User;
import com.andmark.quotegen.domain.enums.UserRole;
import com.andmark.quotegen.dto.UserDTO;
import com.andmark.quotegen.repository.UsersRepository;
import com.andmark.quotegen.service.RegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {
    private final UsersRepository usersRepository;
    private final ModelMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationServiceImpl(UsersRepository usersRepository, ModelMapper mapper, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Long registerNewUser(UserDTO userDTO) {
        log.debug("starting registration new user");
        // Checking for the existence of a user with this name
        if (usersRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Пользователь с таким логином уже существует");
        }

        String encode = passwordEncoder.encode(userDTO.getPassword());

        userDTO.setPassword(encode);
        userDTO.setRole(UserRole.ROLE_USER);

        log.debug("userDTO = {}", userDTO);

        log.debug("registration service starting to save new user");
        usersRepository.save(mapper.map(userDTO, User.class));
        log.debug("user with name = {} is registered", userDTO.getUsername());

        return mapper.map(userDTO,User.class).getId();
    }
}
