package ge.asterbit.assignment.service.impl;

import ge.asterbit.assignment.dto.task.CreateTaskRequest;
import ge.asterbit.assignment.dto.task.TaskDTO;
import ge.asterbit.assignment.dto.task.UpdateTaskRequest;
import ge.asterbit.assignment.entity.*;
import ge.asterbit.assignment.exception.AccessDeniedException;
import ge.asterbit.assignment.exception.ResourceNotFoundException;
import ge.asterbit.assignment.mapper.TaskMapper;
import ge.asterbit.assignment.repository.TaskRepository;
import ge.asterbit.assignment.service.ProjectService;
import ge.asterbit.assignment.service.TaskService;
import ge.asterbit.assignment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserService userService;
    private final ProjectService projectService;

    @Override
    public Page<TaskDTO> getAllTasks(Pageable pageable) {
        User currentUser = userService.getCurrentUserEntity();
        
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only administrators can access all tasks");
        }
        
        return taskRepository.findAll(pageable)
                .map(taskMapper::toDTO);
    }

    @Override
    public Page<TaskDTO> getTasksByProject(Long projectId, Pageable pageable) {
        User currentUser = userService.getCurrentUserEntity();
        Project project = findProjectAndCheckAccess(projectId, currentUser);
        
        return taskRepository.findByProject(project, pageable)
                .map(taskMapper::toDTO);
    }

    @Override
    public Page<TaskDTO> getMyTasks(Pageable pageable) {
        User currentUser = userService.getCurrentUserEntity();
        
        return taskRepository.findByAssignedUser(currentUser, pageable)
                .map(taskMapper::toDTO);
    }

    @Override
    public Page<TaskDTO> getTasksByProjectAndStatus(Long projectId, TaskStatus status, Pageable pageable) {
        User currentUser = userService.getCurrentUserEntity();
        Project project = findProjectAndCheckAccess(projectId, currentUser);
        
        return taskRepository.findByProjectAndStatus(project, status, pageable)
                .map(taskMapper::toDTO);
    }

    @Override
    public Page<TaskDTO> getTasksByProjectAndPriority(Long projectId, TaskPriority priority, Pageable pageable) {
        User currentUser = userService.getCurrentUserEntity();
        Project project = findProjectAndCheckAccess(projectId, currentUser);
        
        return taskRepository.findByProjectAndPriority(project, priority, pageable)
                .map(taskMapper::toDTO);
    }

    @Override
    public TaskDTO getTaskById(Long id) {
        User currentUser = userService.getCurrentUserEntity();
        
        Task task = findTaskAndCheckAccess(id, currentUser);
        return taskMapper.toDTO(task);
    }

    @Override
    @Transactional
    public TaskDTO createTask(CreateTaskRequest request) {
        User currentUser = userService.getCurrentUserEntity();
        
        Project project = findProjectAndCheckAccess(request.getProjectId(), currentUser);
        
        Task task = taskMapper.createRequestToTask(request);
        task.setProject(project);
        
        if (request.getAssignedUserId() != null) {
            User assignedUser = userService.getUserEntityById(request.getAssignedUserId());
            task.setAssignedUser(assignedUser);
        }
        
        Task savedTask = taskRepository.save(task);
        return taskMapper.toDTO(savedTask);
    }

    @Override
    @Transactional
    public TaskDTO updateTask(Long id, UpdateTaskRequest request) {
        User currentUser = userService.getCurrentUserEntity();
        
        Task task = findTaskAndCheckAccess(id, currentUser);
        
        if (!canModifyTask(task, currentUser)) {
            throw new AccessDeniedException("You don't have permission to update this task");
        }
        
        taskMapper.updateTaskFromRequest(request, task);
        
        if (request.getAssignedUserId() != null) {
            if (currentUser.getRole() == Role.USER) {
                throw new AccessDeniedException("Regular users cannot assign tasks");
            }
            
            User assignedUser = userService.getUserEntityById(request.getAssignedUserId());
            task.setAssignedUser(assignedUser);
        }
        
        Task updatedTask = taskRepository.save(task);
        return taskMapper.toDTO(updatedTask);
    }

    @Override
    @Transactional
    public TaskDTO updateTaskStatus(Long id, TaskStatus status) {
        User currentUser = userService.getCurrentUserEntity();
        
        Task task = findTaskAndCheckAccess(id, currentUser);
        
        if (task.getAssignedUser() == null || !task.getAssignedUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the assigned user can update task status");
        }
        
        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);
        
        return taskMapper.toDTO(updatedTask);
    }

    @Override
    @Transactional
    public TaskDTO assignTask(Long id, Long userId) {
        User currentUser = userService.getCurrentUserEntity();
        
        if (currentUser.getRole() == Role.USER) {
            throw new AccessDeniedException("Regular users cannot assign tasks");
        }
        
        Task task = findTaskAndCheckAccess(id, currentUser);
        
        User assignedUser = userService.getUserEntityById(userId);
        
        task.setAssignedUser(assignedUser);
        Task updatedTask = taskRepository.save(task);
        
        return taskMapper.toDTO(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        User currentUser = userService.getCurrentUserEntity();
        
        Task task = findTaskAndCheckAccess(id, currentUser);
        
        if (!canModifyTask(task, currentUser)) {
            throw new AccessDeniedException("You don't have permission to delete this task");
        }
        
        taskRepository.delete(task);
    }
    
    private Project findProjectAndCheckAccess(Long projectId, User user) {
        Project project;
        
        if (user.getRole() == Role.ADMIN) {
            project = projectService.getProjectEntityById(projectId);
        } else {
            project = projectService.getProjectEntityByIdAndOwner(projectId, user);
        }
        
        return project;
    }
    
    private Task findTaskAndCheckAccess(Long taskId, User user) {
        Task task;
        
        if (user.getRole() == Role.ADMIN) {
            task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        } else if (user.getRole() == Role.MANAGER) {
            task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
            
            if (task.getProject().getOwner().getId().equals(user.getId())) {
                return task;
            }
            
            throw new AccessDeniedException("You don't have permission to access this task");
        } else {
            task = taskRepository.findByIdAndAssignedUser(taskId, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        }
        
        return task;
    }
    
    private boolean canModifyTask(Task task, User user) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        
        if (user.getRole() == Role.MANAGER && task.getProject().getOwner().getId().equals(user.getId())) {
            return true;
        }
        
        return false;
    }
} 