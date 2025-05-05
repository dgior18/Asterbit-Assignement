package ge.asterbit.assignment.service;

import ge.asterbit.assignment.dto.user.UserDTO;
import ge.asterbit.assignment.entity.Role;
import ge.asterbit.assignment.entity.User;
import ge.asterbit.assignment.exception.AccessDeniedException;
import ge.asterbit.assignment.exception.ResourceNotFoundException;
import ge.asterbit.assignment.mapper.UserMapper;
import ge.asterbit.assignment.repository.UserRepository;
import ge.asterbit.assignment.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private UserServiceImpl userService;

    private User adminUser;
    private User managerUser;
    private User regularUser;
    private UserDTO adminUserDTO;
    private UserDTO managerUserDTO;
    private UserDTO regularUserDTO;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, userMapper);
        
        // Mock security context
        SecurityContextHolder.setContext(securityContext);
        
        // Create test users
        adminUser = createTestUser(1L, "admin@test.com", Role.ADMIN);
        managerUser = createTestUser(2L, "manager@test.com", Role.MANAGER);
        regularUser = createTestUser(3L, "user@test.com", Role.USER);
        
        // Create test user DTOs
        adminUserDTO = createTestUserDTO(1L, "admin@test.com", Role.ADMIN);
        managerUserDTO = createTestUserDTO(2L, "manager@test.com", Role.MANAGER);
        regularUserDTO = createTestUserDTO(3L, "user@test.com", Role.USER);
    }

    @Test
    void getAllUsers_ShouldReturnPageOfUserDTOs() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(adminUser, managerUser, regularUser));

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toDTO(adminUser)).thenReturn(adminUserDTO);
        when(userMapper.toDTO(managerUser)).thenReturn(managerUserDTO);
        when(userMapper.toDTO(regularUser)).thenReturn(regularUserDTO);

        // Act
        Page<UserDTO> result = userService.getAllUsers(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(adminUserDTO, result.getContent().get(0));
        assertEquals(managerUserDTO, result.getContent().get(1));
        assertEquals(regularUserDTO, result.getContent().get(2));

        verify(userRepository).findAll(pageable);
        verify(userMapper, times(3)).toDTO(any(User.class));
    }

    @Test
    void getUserById_ShouldReturnUserDTO_WhenUserExists() {
        // Arrange
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));
        when(userMapper.toDTO(adminUser)).thenReturn(adminUserDTO);

        // Act
        UserDTO result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(adminUserDTO, result);

        verify(userRepository).findById(userId);
        verify(userMapper).toDTO(adminUser);
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(userId);
        });

        assertTrue(exception.getMessage().contains("User"));
        assertTrue(exception.getMessage().contains(userId.toString()));

        verify(userRepository).findById(userId);
        verify(userMapper, never()).toDTO(any(User.class));
    }

    @Test
    void getCurrentUser_ShouldReturnCurrentUserDTO() {
        // Arrange
        String email = "admin@test.com";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));
        when(userMapper.toDTO(adminUser)).thenReturn(adminUserDTO);

        // Act
        UserDTO result = userService.getCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals(adminUserDTO, result);

        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(userRepository).findByEmail(email);
        verify(userMapper).toDTO(adminUser);
    }

    @Test
    void getCurrentUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String email = "nonexistent@test.com";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getCurrentUser();
        });

        assertEquals("User not found", exception.getMessage());

        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(userRepository).findByEmail(email);
        verify(userMapper, never()).toDTO(any(User.class));
    }

    @Test
    void assignRole_ShouldUpdateUserRoleAndReturnDTO() {
        // Arrange
        Long userId = 3L;
        Role newRole = Role.MANAGER;

        User updatedUser = createTestUser(userId, regularUser.getEmail(), newRole);
        UserDTO updatedUserDTO = createTestUserDTO(userId, regularUser.getEmail(), newRole);

        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDTO(updatedUser)).thenReturn(updatedUserDTO);

        // Act
        UserDTO result = userService.assignRole(userId, newRole);

        // Assert
        assertNotNull(result);
        assertEquals(updatedUserDTO, result);
        assertEquals(newRole, result.getRole());

        verify(userRepository).findById(userId);
        verify(userRepository).save(regularUser);
        verify(userMapper).toDTO(updatedUser);
    }

    @Test
    void getUserEntityById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));

        // Act
        User result = userService.getUserEntityById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(adminUser, result);

        verify(userRepository).findById(userId);
    }

    @Test
    void getUserEntityById_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserEntityById(userId);
        });

        assertTrue(exception.getMessage().contains("User"));
        assertTrue(exception.getMessage().contains(userId.toString()));

        verify(userRepository).findById(userId);
    }

    @Test
    void getCurrentUserEntity_ShouldReturnCurrentUser() {
        // Arrange
        String email = "admin@test.com";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));

        // Act
        User result = userService.getCurrentUserEntity();

        // Assert
        assertNotNull(result);
        assertEquals(adminUser, result);

        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getCurrentUserEntity_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String email = "nonexistent@test.com";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getCurrentUserEntity();
        });

        assertEquals("User not found", exception.getMessage());

        verify(securityContext).getAuthentication();
        verify(authentication).getName();
        verify(userRepository).findByEmail(email);
    }

    private User createTestUser(Long id, String email, Role role) {
        return User.builder()
                .id(id)
                .email(email)
                .password("encodedPassword")
                .role(role)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }

    private UserDTO createTestUserDTO(Long id, String email, Role role) {
        return UserDTO.builder()
                .id(id)
                .email(email)
                .role(role)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }
} 