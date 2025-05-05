package ge.asterbit.assignment.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private String ownerEmail;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
} 