package ge.asterbit.assignment.service;

import ge.asterbit.assignment.dto.user.UserDTO;
import ge.asterbit.assignment.entity.Role;
import ge.asterbit.assignment.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserDTO> getAllUsers(Pageable pageable);
    UserDTO getUserById(Long id);
    UserDTO getCurrentUser();
    UserDTO assignRole(Long userId, Role role);
    
    User getUserEntityById(Long id);
    User getCurrentUserEntity();
} 