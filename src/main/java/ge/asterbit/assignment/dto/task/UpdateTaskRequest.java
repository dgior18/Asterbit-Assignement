package ge.asterbit.assignment.dto.task;

import ge.asterbit.assignment.entity.TaskPriority;
import ge.asterbit.assignment.entity.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {

    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private TaskStatus status;

    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;

    private TaskPriority priority;

    private Long assignedUserId;
} 