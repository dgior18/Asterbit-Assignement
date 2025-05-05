package ge.asterbit.assignment.service.impl;

import ge.asterbit.assignment.dto.user.UserDTO;
import ge.asterbit.assignment.entity.Role;
import ge.asterbit.assignment.entity.User;
import ge.asterbit.assignment.exception.ResourceNotFoundException;
import ge.asterbit.assignment.mapper.UserMapper;
import ge.asterbit.assignment.repository.UserRepository;
import ge.asterbit.assignment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDTO);
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toDTO(user);
    }
    
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Override
    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return userMapper.toDTO(user);
    }
    
    public User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    @Override
    @Transactional
    public UserDTO assignRole(Long userId, Role role) {
        User user = getUserEntityById(userId);
        user.setRole(role);
        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }
} 