package ge.asterbit.assignment.service;

import ge.asterbit.assignment.dto.auth.AuthRequest;
import ge.asterbit.assignment.dto.auth.AuthResponse;
import ge.asterbit.assignment.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse authenticate(AuthRequest request);
} 