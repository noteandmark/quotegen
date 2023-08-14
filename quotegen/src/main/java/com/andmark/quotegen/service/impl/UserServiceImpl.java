package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.domain.Book;
import com.andmark.quotegen.domain.User;
import com.andmark.quotegen.domain.enums.UserRole;
import com.andmark.quotegen.dto.UserDTO;
import com.andmark.quotegen.repository.UsersRepository;
import com.andmark.quotegen.service.UserService;
import com.andmark.quotegen.util.impl.MapperConvert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {
    private final UsersRepository usersRepository;
    private final MapperConvert<User, UserDTO> mapper;

    @Autowired
    public UserServiceImpl(UsersRepository usersRepository, MapperConvert<User, UserDTO> mapper) {
        this.usersRepository = usersRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(UserDTO userDTO) {
        log.debug("saving book");
        usersRepository.save(convertToEntity(userDTO));
        log.info("save book {}", userDTO);
    }

    @Override
    public UserDTO findOne(Long id) {
        log.debug("find user by id {}", id);
        Optional<User> foundUser = usersRepository.findById(id);
        log.info("find user {}", foundUser);
        return foundUser.map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public List<UserDTO> findAll() {
        log.debug("find all users");
        List<User> userList = usersRepository.findAll();
        log.info("founded userList = {}", userList);
        return convertToDtoList(userList);
    }

    @Override
    @Transactional
    public void update(Long id, UserDTO updatedUserDTO) {
        log.debug("update user by id {}", id);
        User updatedUser = convertToEntity(updatedUserDTO);
        updatedUser.setId(id);
        usersRepository.save(updatedUser);
        log.info("update user {}", updatedUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("delete user by id {}", id);
        usersRepository.deleteById(id);
        log.info("delete user with id {} perform", id);
    }

    public void registerUser(String username, String password, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        usersRepository.save(user);
    }

    private UserDTO convertToDTO(User user) {
        return mapper.convertToDTO(user, UserDTO.class);
    }

    private User convertToEntity(UserDTO userDTO) {
        return mapper.convertToEntity(userDTO, Quote.class);
    }

    private List<UserDTO> convertToDtoList(List<User> users) {
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
