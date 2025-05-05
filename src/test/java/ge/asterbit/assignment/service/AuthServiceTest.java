package ge.asterbit.assignment.service;

import ge.asterbit.assignment.dto.auth.AuthRequest;
import ge.asterbit.assignment.dto.auth.AuthResponse;
import ge.asterbit.assignment.dto.auth.RegisterRequest;
import ge.asterbit.assignment.entity.Role;
import ge.asterbit.assignment.entity.User;
import ge.asterbit.assignment.mapper.UserMapper;
import ge.asterbit.assignment.repository.UserRepository;
import ge.asterbit.assignment.security.JwtService;
import ge.asterbit.assignment.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                userMapper,
                passwordEncoder,
                jwtService,
                authenticationManager
        );
    }

    @Test
    void register_ShouldCreateUserAndReturnTokenResponse() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        User mappedUser = User.builder()
                .email(request.getEmail())
                .role(Role.USER)
                .build();

        User savedUser = User.builder()
                .id(1L)
                .email(request.getEmail())
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        String token = "jwt-token";

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.registerRequestToUser(any(RegisterRequest.class))).thenReturn(mappedUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn(token);

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(token, response.getToken());
        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(Role.USER, response.getRole());

        verify(userRepository).existsByEmail(request.getEmail());
        verify(userMapper).registerRequestToUser(request);
        verify(passwordEncoder).encode(request.getPassword());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals("encodedPassword", capturedUser.getPassword());

        verify(jwtService).generateToken(savedUser);
    }

    @Test
    void authenticate_ShouldAuthenticateAndReturnTokenResponse() {
        // Arrange
        AuthRequest request = AuthRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        User user = User.builder()
                .id(1L)
                .email(request.getEmail())
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        String token = "jwt-token";

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn(token);

        // Act
        AuthResponse response = authService.authenticate(request);

        // Assert
        assertNotNull(response);
        assertEquals(token, response.getToken());
        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(Role.USER, response.getRole());

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        verify(userRepository).findByEmail(request.getEmail());
        verify(jwtService).generateToken(user);
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register(request);
        });

        assertEquals("Email address is already registered. Please use a different email.", exception.getMessage());
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userMapper, never()).registerRequestToUser(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }
} 