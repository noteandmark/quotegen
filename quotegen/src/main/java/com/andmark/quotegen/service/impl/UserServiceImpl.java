package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.domain.User;
import com.andmark.quotegen.domain.enums.UserRole;
import com.andmark.quotegen.dto.UserDTO;
import com.andmark.quotegen.repository.UsersRepository;
import com.andmark.quotegen.service.UserService;
import com.andmark.quotegen.util.impl.MapperConvert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UsersRepository usersRepository;
    private final MapperConvert<User, UserDTO> mapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UsersRepository usersRepository, MapperConvert<User, UserDTO> mapper, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void save(UserDTO userDTO) {
        log.debug("saving user");
        usersRepository.save(convertToEntity(userDTO));
        log.info("save user {}", userDTO);
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

    @Override
    public boolean isRegistered(Long usertgId) {
        log.debug("check if usertgId = {} exists", usertgId);
        return usersRepository.existsByUsertgId(usertgId);
    }

    @Override
    public boolean existsByUsername(String username) {
        log.debug("check if exists username = {}", username);
        return usersRepository.existsByUsername(username);
    }

    @Override
    public UserRole getUserRole(Long usertgId) {
        log.debug("user service getUserRole");
        User user = usersRepository.findByUsertgId(usertgId);
        if (user != null) {
            return user.getRole();
        }
        return null; // User not found
    }

    @Override
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        log.debug("user service changePassword");

        User user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Check if the current password matches the password in the database
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Incorrect current password");
            throw new IllegalArgumentException("Incorrect current password");
        }

        // Coding and setting a new password
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);

        // Save the user with a new password
        usersRepository.save(user);
        log.debug("Password changed successfully for user: {}", username);
    }

    @Override
    public UserDTO findByUsername(String username) {
        log.debug("user service findByUsername = {}", username);
        Optional<User> byUsername = usersRepository.findByUsername(username);
        log.info("find byUsername = {}", byUsername);
        return byUsername.map(this::convertToDTO).orElse(null);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("in method loadUserByUsername find username: {}", username);

        UserDTO userDTO = findByUsername(username);

        if (userDTO == null) {
            log.debug("user not found with username: {}", username);
            throw new UsernameNotFoundException("User not found with username: " + username);
        } else {
            log.debug("found userDTO = {}", userDTO);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(userDTO.getUsername())
                .password(userDTO.getPassword())
                .authorities(userDTO.getRole().name()) // Добавление роли как SimpleGrantedAuthority
                .disabled(false)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }

    private UserDTO convertToDTO(User user) {
        return mapper.convertToDTO(user, UserDTO.class);
    }

    User convertToEntity(UserDTO userDTO) {
        return mapper.convertToEntity(userDTO, User.class);
    }

    private List<UserDTO> convertToDtoList(List<User> users) {
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


}
