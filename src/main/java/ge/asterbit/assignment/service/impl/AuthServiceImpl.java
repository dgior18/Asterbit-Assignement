package ge.asterbit.assignment.service.impl;

import ge.asterbit.assignment.dto.auth.AuthRequest;
import ge.asterbit.assignment.dto.auth.AuthResponse;
import ge.asterbit.assignment.dto.auth.RegisterRequest;
import ge.asterbit.assignment.entity.Role;
import ge.asterbit.assignment.entity.User;
import ge.asterbit.assignment.exception.ResourceNotFoundException;
import ge.asterbit.assignment.mapper.UserMapper;
import ge.asterbit.assignment.repository.UserRepository;
import ge.asterbit.assignment.security.JwtService;
import ge.asterbit.assignment.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email address is already registered. Please use a different email.");
        }
        
        User user = userMapper.registerRequestToUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        
        User savedUser = userRepository.save(user);
        String jwtToken = jwtService.generateToken(savedUser);
        
        return AuthResponse.builder()
                .token(jwtToken)
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }
    
    @Override
    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with the provided email address"));
        
        String jwtToken = jwtService.generateToken(user);
        
        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
} 