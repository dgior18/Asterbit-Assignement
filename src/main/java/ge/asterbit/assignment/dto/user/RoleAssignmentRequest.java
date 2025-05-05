package ge.asterbit.assignment.dto.user;

import ge.asterbit.assignment.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentRequest {
    
    @NotNull(message = "Role is required")
    private Role role;
} 