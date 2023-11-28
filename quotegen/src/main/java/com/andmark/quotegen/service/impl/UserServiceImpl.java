package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.domain.User;
import com.andmark.quotegen.domain.enums.UserRole;
import com.andmark.quotegen.dto.UserDTO;
import com.andmark.quotegen.repository.UsersRepository;
import com.andmark.quotegen.service.UserService;
import com.andmark.quotegen.util.impl.MapperConvert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @Autowired
    public UserServiceImpl(UsersRepository usersRepository, MapperConvert<User, UserDTO> mapper) {
        this.usersRepository = usersRepository;
        this.mapper = mapper;
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
        User user = usersRepository.findByUsertgId(usertgId);
        if (user != null) {
            return user.getRole();
        }
        return null; // User not found
    }

    @Override
    public UserDTO findByUsername(String username) {
        log.debug("user service findByUsername = {}", username);
        Optional<User> byUsername = usersRepository.findByUsername(username);
        log.info("find byUsername = {}", byUsername);
        return byUsername.map(this::convertToDTO).orElse(null);
    }

    @Override
    public boolean isAuthenticated(Authentication authentication) {
        log.debug("user service: isAuthenticated");
        if (authentication != null)
            System.out.println("auth = " + authentication.toString());
        if (authentication != null && authentication.isAuthenticated()) {
            log.debug("isAuthenticated true");
        } else {
            log.debug("isAuthenticated false");
        }
        return authentication != null && authentication.isAuthenticated();
    }

    @Override
    public boolean isAdmin(Authentication authentication) {
        log.debug("user service: isAdmin");
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.debug("isAdmin: {}", userDetails);
        return userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    @Override
    public boolean isUser(Authentication authentication) {
        log.debug("user service: isUser");
        log.debug("authentication = {}", authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.debug("isUser: {}", userDetails);
        return userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER"));

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
