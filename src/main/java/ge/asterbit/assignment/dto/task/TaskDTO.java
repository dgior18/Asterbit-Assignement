package ge.asterbit.assignment.dto.task;

import ge.asterbit.assignment.entity.TaskPriority;
import ge.asterbit.assignment.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate dueDate;
    private TaskPriority priority;
    private Long projectId;
    private String projectName;
    private Long assignedUserId;
    private String assignedUserEmail;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
} 