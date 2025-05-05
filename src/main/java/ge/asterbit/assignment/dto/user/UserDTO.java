package ge.asterbit.assignment.dto.user;

import ge.asterbit.assignment.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private Role role;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
} 