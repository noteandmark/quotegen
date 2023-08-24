package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.domain.User;
import com.andmark.quotegen.domain.enums.UserRole;
import com.andmark.quotegen.dto.UserDTO;
import com.andmark.quotegen.repository.UsersRepository;
import com.andmark.quotegen.util.impl.MapperConvert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private MapperConvert<User, UserDTO> mapper;
    @InjectMocks
    private UserServiceImpl userService;

    private UserDTO userDTO;
    private User user;
    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        userDTO = new UserDTO();
        user = new User();
        user.setId(userId);
        lenient().when(mapper.convertToDTO(user, UserDTO.class)).thenReturn(userDTO);
        lenient().when(mapper.convertToEntity(userDTO, User.class)).thenReturn(user);
    }

    @Test
    void testSave() {
        // Mocking behavior
        when(userService.convertToEntity(userDTO)).thenReturn(user);
        // Test
        userService.save(userDTO);
        // Verification
        verify(usersRepository).save(user);
    }

    @Test
    void testFindOne() {
        when(usersRepository.findById(userId)).thenReturn(Optional.of(user));
        UserDTO result = userService.findOne(userId);
        assertEquals(userDTO, result);
    }

    @Test
    void testFindAll() {
        List<User> all = new ArrayList<>();
        all.add(user);

        when(usersRepository.findAll()).thenReturn(all);
        userService.findAll();
        verify(usersRepository, only()).findAll();
    }

    @Test
    void testUpdateGreeting() {
        // Mocking
        UserDTO updatedGreetingDTO = new UserDTO();
        updatedGreetingDTO.setUsername("new user");
        User updatedGreeting = new User();
        updatedGreeting.setUsername(updatedGreetingDTO.getUsername());

        // Mocking behavior
        when(userService.convertToEntity(updatedGreetingDTO)).thenReturn(updatedGreeting);
        // Test
        userService.update(userId, updatedGreetingDTO);
        // Verification
        verify(usersRepository).save(updatedGreeting);
    }

    @Test
    void testDelete() {
        // Test
        userService.delete(userId);
        // Verification
        verify(usersRepository).deleteById(userId);
    }

    @Test
    void testIsRegistered() {

        when(usersRepository.existsByUsertgId(userId)).thenReturn(true);

        boolean result = userService.isRegistered(userId);

        assertTrue(result);
    }

    @Test
    void testExistsByUsername() {
        String username = "testUser";

        when(usersRepository.existsByUsername(username)).thenReturn(true);

        boolean result = userService.existsByUsername(username);

        assertTrue(result);
    }

    @Test
    void testGetUserRole_UserExists() {
        User mockUser = new User();
        mockUser.setRole(UserRole.ADMIN);

        when(usersRepository.findByUsertgId(userId)).thenReturn(mockUser);

        UserRole result = userService.getUserRole(userId);

        assertEquals(UserRole.ADMIN, result);
    }

    @Test
    void testGetUserRole_UserNotFound() {

        when(usersRepository.findByUsertgId(userId)).thenReturn(null);

        UserRole result = userService.getUserRole(userId);

        assertNull(result);
    }


}