package ge.asterbit.assignment.controller;

import ge.asterbit.assignment.dto.user.RoleAssignmentRequest;
import ge.asterbit.assignment.dto.user.UserDTO;
import ge.asterbit.assignment.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User Management API")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('admin:read')")
    @Operation(summary = "Get all users (ADMIN only)")
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:read')")
    @Operation(summary = "Get user by ID (ADMIN only)")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserDTO> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }
    
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAuthority('admin:update')")
    @Operation(summary = "Assign role to user (ADMIN only)")
    public ResponseEntity<UserDTO> assignRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleAssignmentRequest request) {
        return ResponseEntity.ok(userService.assignRole(id, request.getRole()));
    }
} 