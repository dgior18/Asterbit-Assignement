package ge.asterbit.assignment.service;

import ge.asterbit.assignment.dto.task.CreateTaskRequest;
import ge.asterbit.assignment.dto.task.TaskDTO;
import ge.asterbit.assignment.dto.task.UpdateTaskRequest;
import ge.asterbit.assignment.entity.TaskPriority;
import ge.asterbit.assignment.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    Page<TaskDTO> getAllTasks(Pageable pageable);
    Page<TaskDTO> getTasksByProject(Long projectId, Pageable pageable);
    Page<TaskDTO> getMyTasks(Pageable pageable);
    Page<TaskDTO> getTasksByProjectAndStatus(Long projectId, TaskStatus status, Pageable pageable);
    Page<TaskDTO> getTasksByProjectAndPriority(Long projectId, TaskPriority priority, Pageable pageable);
    TaskDTO getTaskById(Long id);
    TaskDTO createTask(CreateTaskRequest request);
    TaskDTO updateTask(Long id, UpdateTaskRequest request);
    TaskDTO updateTaskStatus(Long id, TaskStatus status);
    TaskDTO assignTask(Long id, Long userId);
    void deleteTask(Long id);
} 