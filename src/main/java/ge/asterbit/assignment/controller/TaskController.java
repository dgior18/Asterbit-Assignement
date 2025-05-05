package ge.asterbit.assignment.controller;

import ge.asterbit.assignment.dto.task.CreateTaskRequest;
import ge.asterbit.assignment.dto.task.TaskDTO;
import ge.asterbit.assignment.dto.task.UpdateTaskRequest;
import ge.asterbit.assignment.entity.TaskPriority;
import ge.asterbit.assignment.entity.TaskStatus;
import ge.asterbit.assignment.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task Management API")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @PreAuthorize("hasAuthority('admin:read')")
    @Operation(summary = "Get all tasks (ADMIN only)")
    public ResponseEntity<Page<TaskDTO>> getAllTasks(Pageable pageable) {
        return ResponseEntity.ok(taskService.getAllTasks(pageable));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's assigned tasks")
    public ResponseEntity<Page<TaskDTO>> getMyTasks(Pageable pageable) {
        return ResponseEntity.ok(taskService.getMyTasks(pageable));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get tasks by project")
    public ResponseEntity<Page<TaskDTO>> getTasksByProject(
            @PathVariable Long projectId, Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId, pageable));
    }

    @GetMapping("/project/{projectId}/status/{status}")
    @Operation(summary = "Get tasks by project and status")
    public ResponseEntity<Page<TaskDTO>> getTasksByProjectAndStatus(
            @PathVariable Long projectId,
            @PathVariable TaskStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasksByProjectAndStatus(projectId, status, pageable));
    }

    @GetMapping("/project/{projectId}/priority/{priority}")
    @Operation(summary = "Get tasks by project and priority")
    public ResponseEntity<Page<TaskDTO>> getTasksByProjectAndPriority(
            @PathVariable Long projectId,
            @PathVariable TaskPriority priority,
            Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasksByProjectAndPriority(projectId, priority, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return new ResponseEntity<>(taskService.createTask(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @PatchMapping("/{id}/status/{status}")
    @Operation(summary = "Update task status")
    public ResponseEntity<TaskDTO> updateTaskStatus(
            @PathVariable Long id,
            @PathVariable TaskStatus status) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, status));
    }

    @PatchMapping("/{id}/assign/{userId}")
    @PreAuthorize("hasAnyAuthority('admin:update', 'manager:update')")
    @Operation(summary = "Assign task to user (ADMIN and MANAGER only)")
    public ResponseEntity<TaskDTO> assignTask(
            @PathVariable Long id,
            @PathVariable Long userId) {
        return ResponseEntity.ok(taskService.assignTask(id, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
} 