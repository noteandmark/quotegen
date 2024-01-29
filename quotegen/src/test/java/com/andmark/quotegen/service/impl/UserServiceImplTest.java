package com.andmark.quotegen.service.impl;

import com.andmark.quotegen.domain.User;
import com.andmark.quotegen.domain.enums.UserRole;
import com.andmark.quotegen.dto.UserDTO;
import com.andmark.quotegen.exception.ServiceException;
import com.andmark.quotegen.repository.UsersRepository;
import com.andmark.quotegen.util.MapperConvert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private MapperConvert<User, UserDTO> mapper;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDTO userDTO;
    private User user;
    private Long userId;


    @BeforeEach
    void setUp() {

        userId = 1L;
        String currentPassword = "oldPassword";
        String encodedCurrentPassword = "$2a$10$1BnrAJWw0tyHPnWk1m88ReKGJWVeiXyON0USD2Yx51vlc91KzlZLi";
        String username = "testUser";

        userDTO = new UserDTO();
        userDTO.setId(userId);
        userDTO.setUsername(username);
        userDTO.setPassword(encodedCurrentPassword);
        user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setPassword(encodedCurrentPassword);

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
    void testFindOneByUsertgId_WhenUserExists() {
        when(usersRepository.findByUsertgId(userId)).thenReturn(Optional.of(user));
        UserDTO result = userService.findOneByUsertgId(userId);
        assertEquals(userDTO, result);
    }

    @Test
    void testFindOneByUsertgId_WhenUserDoesNotExist() {
        // Arrange
        Long usertgId = 123L;
        when(usersRepository.findByUsertgId(usertgId)).thenReturn(Optional.empty());
        // Act
        UserDTO result = userService.findOneByUsertgId(usertgId);
        // Assert
        assertNull(result);
    }

    @Test
    void testFindByUsername_UserFound() {
        // Arrange
        String username = "testUser";
        when(usersRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserDTO result = userService.findByUsername(username);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(usersRepository).findByUsername(username);
    }

    @Test
    void testFindByUsername_UserNotFound() {
        // Arrange
        String username = "nonExistingUser";
        when(usersRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        UserDTO result = userService.findByUsername(username);

        // Assert
        assertNull(result);
        verify(usersRepository).findByUsername(username);
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
    void testUpdateGreeting_whenUserIsFound_thenUpdateNickname() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testuser");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(mapper.convertToEntity(userDTO, User.class)).thenReturn(user);

        // Act
        UserDTO result = userService.update(userDTO);

        // Assert
        assertEquals(userDTO, result);
        verify(usersRepository, times(1)).save(user);
    }

    @Test
    void testUpdateNickname() {
        // Arrange
        String username = "testUser";
        String newNickname = "NewNickname";
        userDTO.setUsername(username);
        userDTO.setNickname(newNickname);

        when(usersRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserDTO result = userService.updateNickname(userDTO);
        // Assert
        verify(usersRepository).save(user);
        assertEquals(newNickname, result.getNickname());
    }

    @Test
    void testDelete() {
        // Test
        userService.delete(userId);
        // Verification
        verify(usersRepository).deleteById(userId);
    }

    @Test
    void testDeleteByUsertgId() {
        // Test
        userService.delete(userId);
        // Verification
        verify(usersRepository).deleteById(userId);
    }

    @Test
    void testDeleteByUsername() {
        // Arrange
        String username = "testUser";
        user.setUsername(username);
        when(usersRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        userService.deleteByUsername(username);

        // Assert
        verify(usersRepository).findByUsername(username);
        verify(usersRepository).delete(user);
    }

    @Test
    void testDeleteByUsername_whenUserNotFound() {
        // Arrange
        String username = "nonExistingUser";
        when(usersRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ServiceException.class, () -> userService.deleteByUsername(username));
        verify(usersRepository).findByUsername(username);
        verifyNoMoreInteractions(usersRepository);
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
        mockUser.setRole(UserRole.ROLE_ADMIN);

        when(usersRepository.findByUsertgId(userId)).thenReturn(Optional.of(mockUser));

        UserRole result = userService.getUserRole(userId);

        assertEquals(UserRole.ROLE_ADMIN, result);
    }

    @Test
    void testGetUserRole_UserNotFound() {
        when(usersRepository.findByUsertgId(userId)).thenReturn(Optional.empty());

        UserRole result = userService.getUserRole(userId);

        assertNull(result);
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        // Arrange
        String username = "testUser";
        String currentPassword = "$2a$10$1BnrAJWw0tyHPnWk1m88ReKGJWVeiXyON0USD2Yx51vlc91KzlZLi";
        String newPassword = "newPassword";
        String encodedNewPassword = "$3a$10$1BnrAJWw0tyHPnWk1m88ReKGJWVeiXyON0USD2Yx51vlc91KzlZLi";

        when(usersRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("$3a$10$1BnrAJWw0tyHPnWk1m88ReKGJWVeiXyON0USD2Yx51vlc91KzlZLi");
        when(usersRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.changePassword(username, currentPassword, newPassword);
        // Assert
        verify(usersRepository, times(1)).save(user);
        assertEquals(encodedNewPassword, user.getPassword());
    }


    @Test
    void testChangePassword_UserNotFound() {
        // Arrange
        String username = "nonExistentUser";

        when(usersRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> userService.changePassword(username, "oldPassword", "newPassword"));
        verify(usersRepository).findByUsername(username);
        verify(usersRepository, never()).save(any());
    }

    @Test
    void testChangePassword_IncorrectCurrentPassword() {
        // Arrange
        String username = "testUser";
        String currentPassword = "wrongPassword";
        String newPassword = "newPassword";

        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("correctPassword"));

        when(usersRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.changePassword(username, currentPassword, newPassword));
        verify(usersRepository).findByUsername(username);
        verify(usersRepository, never()).save(any());
    }

    @Test
    void shouldLoadUserByUsernameSuccessfully() {
        // Arrange
        String username = "testUser";
        String encodedPassword = "$2a$10$1BnrAJWw0tyHPnWk1m88ReKGJWVeiXyON0USD2Yx51vlc91KzlZLi";
        System.out.println("encodedPassword = " + encodedPassword);
        UserRole userRole = UserRole.ROLE_USER;

        userDTO.setPassword(encodedPassword);
        userDTO.setRole(userRole);
        user.setPassword(encodedPassword);
        user.setRole(userRole);

        when(usersRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = userService.loadUserByUsername(username);

        // Assert
        assertEquals(username, userDetails.getUsername());
        assertEquals(encodedPassword, userDetails.getPassword());
        assertEquals("ROLE_USER", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenUserNotFound() {
        // Arrange
        String username = "nonExistentUser";

        when(usersRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(username));
    }


}