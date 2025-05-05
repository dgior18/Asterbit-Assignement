package ge.asterbit.assignment.service;

import ge.asterbit.assignment.dto.task.CreateTaskRequest;
import ge.asterbit.assignment.dto.task.TaskDTO;
import ge.asterbit.assignment.dto.task.UpdateTaskRequest;
import ge.asterbit.assignment.entity.*;
import ge.asterbit.assignment.exception.AccessDeniedException;
import ge.asterbit.assignment.exception.ResourceNotFoundException;
import ge.asterbit.assignment.mapper.TaskMapper;
import ge.asterbit.assignment.repository.TaskRepository;
import ge.asterbit.assignment.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private UserService userService;

    @Mock
    private ProjectService projectService;

    private TaskServiceImpl taskService;

    private User adminUser;
    private User managerUser;
    private User regularUser;
    private Project project;
    private Task task;
    private TaskDTO taskDTO;

    @BeforeEach
    void setUp() {
        taskService = new TaskServiceImpl(taskRepository, taskMapper, userService, projectService);

        adminUser = createTestUser(1L, "admin@test.com", Role.ADMIN);
        managerUser = createTestUser(2L, "manager@test.com", Role.MANAGER);
        regularUser = createTestUser(3L, "user@test.com", Role.USER);
        project = createTestProject(1L, "Test Project", managerUser);
        task = createTestTask(1L, "Test Task", project, regularUser);
        taskDTO = createTestTaskDTO(1L, "Test Task", project.getId(), regularUser.getId());
    }

    @Test
    void getAllTasks_ShouldReturnAllTasks_WhenUserIsAdmin() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(userService.getCurrentUserEntity()).thenReturn(adminUser);
        when(taskRepository.findAll(pageable)).thenReturn(taskPage);
        when(taskMapper.toDTO(any(Task.class))).thenReturn(taskDTO);

        // Act
        Page<TaskDTO> result = taskService.getAllTasks(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(taskDTO, result.getContent().get(0));

        verify(userService).getCurrentUserEntity();
        verify(taskRepository).findAll(pageable);
        verify(taskMapper).toDTO(task);
    }

    @Test
    void getAllTasks_ShouldThrowException_WhenUserIsNotAdmin() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        when(userService.getCurrentUserEntity()).thenReturn(regularUser);

        // Act & Assert
        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            taskService.getAllTasks(pageable);
        });

        assertEquals("Only administrators can access all tasks", exception.getMessage());

        verify(userService).getCurrentUserEntity();
        verify(taskRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getTasksByProject_ShouldReturnTasksForProject() {
        // Arrange
        Long projectId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(userService.getCurrentUserEntity()).thenReturn(managerUser);
        when(projectService.getProjectEntityByIdAndOwner(projectId, managerUser)).thenReturn(project);
        when(taskRepository.findByProject(project, pageable)).thenReturn(taskPage);
        when(taskMapper.toDTO(any(Task.class))).thenReturn(taskDTO);

        // Act
        Page<TaskDTO> result = taskService.getTasksByProject(projectId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(taskDTO, result.getContent().get(0));

        verify(userService).getCurrentUserEntity();
        verify(projectService).getProjectEntityByIdAndOwner(projectId, managerUser);
        verify(taskRepository).findByProject(project, pageable);
        verify(taskMapper).toDTO(task);
    }

    @Test
    void getMyTasks_ShouldReturnTasksAssignedToCurrentUser() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(userService.getCurrentUserEntity()).thenReturn(regularUser);
        when(taskRepository.findByAssignedUser(regularUser, pageable)).thenReturn(taskPage);
        when(taskMapper.toDTO(any(Task.class))).thenReturn(taskDTO);

        // Act
        Page<TaskDTO> result = taskService.getMyTasks(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(taskDTO, result.getContent().get(0));

        verify(userService).getCurrentUserEntity();
        verify(taskRepository).findByAssignedUser(regularUser, pageable);
        verify(taskMapper).toDTO(task);
    }

    @Test
    void getTasksByProjectAndStatus_ShouldReturnFilteredTasks() {
        // Arrange
        Long projectId = 1L;
        TaskStatus status = TaskStatus.IN_PROGRESS;
        Pageable pageable = PageRequest.of(0, 10);
        task.setStatus(status);
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(userService.getCurrentUserEntity()).thenReturn(managerUser);
        when(projectService.getProjectEntityByIdAndOwner(projectId, managerUser)).thenReturn(project);
        when(taskRepository.findByProjectAndStatus(project, status, pageable)).thenReturn(taskPage);
        when(taskMapper.toDTO(any(Task.class))).thenReturn(taskDTO);

        // Act
        Page<TaskDTO> result = taskService.getTasksByProjectAndStatus(projectId, status, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(taskDTO, result.getContent().get(0));

        verify(userService).getCurrentUserEntity();
        verify(projectService).getProjectEntityByIdAndOwner(projectId, managerUser);
        verify(taskRepository).findByProjectAndStatus(project, status, pageable);
        verify(taskMapper).toDTO(task);
    }

    @Test
    void getTaskById_ShouldReturnTask_WhenUserIsAdmin() {
        // Arrange
        Long taskId = 1L;

        when(userService.getCurrentUserEntity()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskMapper.toDTO(task)).thenReturn(taskDTO);

        // Act
        TaskDTO result = taskService.getTaskById(taskId);

        // Assert
        assertNotNull(result);
        assertEquals(taskDTO, result);

        verify(userService).getCurrentUserEntity();
        verify(taskRepository).findById(taskId);
        verify(taskMapper).toDTO(task);
    }

    @Test
    void getTaskById_ShouldReturnTask_WhenUserIsAssignedUser() {
        // Arrange
        Long taskId = 1L;

        when(userService.getCurrentUserEntity()).thenReturn(regularUser);
        when(taskRepository.findByIdAndAssignedUser(taskId, regularUser)).thenReturn(Optional.of(task));
        when(taskMapper.toDTO(task)).thenReturn(taskDTO);

        // Act
        TaskDTO result = taskService.getTaskById(taskId);

        // Assert
        assertNotNull(result);
        assertEquals(taskDTO, result);

        verify(userService).getCurrentUserEntity();
        verify(taskRepository).findByIdAndAssignedUser(taskId, regularUser);
        verify(taskMapper).toDTO(task);
    }

    @Test
    void createTask_ShouldCreateAndReturnTaskDTO() {
        // Arrange
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("New Task")
                .description("Task Description")
                .projectId(project.getId())
                .assignedUserId(regularUser.getId())
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        Task newTask = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .status(TaskStatus.TODO)
                .build();

        Task savedTask = Task.builder()
                .id(2L)
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .status(TaskStatus.TODO)
                .project(project)
                .assignedUser(regularUser)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        TaskDTO savedTaskDTO = TaskDTO.builder()
                .id(2L)
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .status(TaskStatus.TODO)
                .projectId(project.getId())
                .assignedUserId(regularUser.getId())
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        when(userService.getCurrentUserEntity()).thenReturn(managerUser);
        when(projectService.getProjectEntityByIdAndOwner(project.getId(), managerUser)).thenReturn(project);
        when(taskMapper.createRequestToTask(request)).thenReturn(newTask);
        when(userService.getUserEntityById(regularUser.getId())).thenReturn(regularUser);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        when(taskMapper.toDTO(savedTask)).thenReturn(savedTaskDTO);

        // Act
        TaskDTO result = taskService.createTask(request);

        // Assert
        assertNotNull(result);
        assertEquals(savedTaskDTO, result);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        Task capturedTask = taskCaptor.getValue();
        assertEquals(request.getTitle(), capturedTask.getTitle());
        assertEquals(project, capturedTask.getProject());
        assertEquals(regularUser, capturedTask.getAssignedUser());

        verify(userService).getCurrentUserEntity();
        verify(projectService).getProjectEntityByIdAndOwner(project.getId(), managerUser);
        verify(taskMapper).createRequestToTask(request);
        verify(userService).getUserEntityById(regularUser.getId());
        verify(taskMapper).toDTO(savedTask);
    }

    @Test
    void updateTaskStatus_ShouldUpdateStatus_WhenUserIsAssignedUser() {
        // Arrange
        Long taskId = 1L;
        TaskStatus newStatus = TaskStatus.DONE;

        Task updatedTask = createTestTask(taskId, task.getTitle(), project, regularUser);
        updatedTask.setStatus(newStatus);

        TaskDTO updatedTaskDTO = createTestTaskDTO(taskId, task.getTitle(), project.getId(), regularUser.getId());
        updatedTaskDTO.setStatus(newStatus);

        when(userService.getCurrentUserEntity()).thenReturn(regularUser);
        when(taskRepository.findByIdAndAssignedUser(taskId, regularUser)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskMapper.toDTO(updatedTask)).thenReturn(updatedTaskDTO);

        // Act
        TaskDTO result = taskService.updateTaskStatus(taskId, newStatus);

        // Assert
        assertNotNull(result);
        assertEquals(updatedTaskDTO, result);
        assertEquals(newStatus, result.getStatus());

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        Task capturedTask = taskCaptor.getValue();
        assertEquals(newStatus, capturedTask.getStatus());

        verify(userService).getCurrentUserEntity();
        verify(taskRepository).findByIdAndAssignedUser(taskId, regularUser);
        verify(taskMapper).toDTO(updatedTask);
    }

    @Test
    void assignTask_ShouldAssignTaskToUser_WhenUserIsManager() {
        // Arrange
        Long taskId = 1L;
        Long userId = regularUser.getId();

        Task updatedTask = createTestTask(taskId, task.getTitle(), project, regularUser);
        TaskDTO updatedTaskDTO = createTestTaskDTO(taskId, task.getTitle(), project.getId(), regularUser.getId());

        when(userService.getCurrentUserEntity()).thenReturn(managerUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userService.getUserEntityById(userId)).thenReturn(regularUser);
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskMapper.toDTO(updatedTask)).thenReturn(updatedTaskDTO);

        // Act
        TaskDTO result = taskService.assignTask(taskId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(updatedTaskDTO, result);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        Task capturedTask = taskCaptor.getValue();
        assertEquals(regularUser, capturedTask.getAssignedUser());

        verify(userService).getCurrentUserEntity();
        verify(taskRepository).findById(taskId);
        verify(userService).getUserEntityById(userId);
        verify(taskMapper).toDTO(updatedTask);
    }

    @Test
    void deleteTask_ShouldDeleteTask_WhenUserIsAdmin() {
        // Arrange
        Long taskId = 1L;

        when(userService.getCurrentUserEntity()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act
        taskService.deleteTask(taskId);

        // Assert
        verify(userService).getCurrentUserEntity();
        verify(taskRepository).findById(taskId);
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_ShouldThrowException_WhenUserIsNotAuthorized() {
        // Arrange
        Long taskId = 1L;
        User anotherUser = createTestUser(4L, "another@test.com", Role.USER);

        when(userService.getCurrentUserEntity()).thenReturn(anotherUser);
        when(taskRepository.findByIdAndAssignedUser(taskId, anotherUser)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.deleteTask(taskId);
        });

        assertTrue(exception.getMessage().contains("Task"));

        verify(userService).getCurrentUserEntity();
        verify(taskRepository).findByIdAndAssignedUser(taskId, anotherUser);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    private User createTestUser(Long id, String email, Role role) {
        return User.builder()
                .id(id)
                .email(email)
                .password("encodedPassword")
                .role(role)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }

    private Project createTestProject(Long id, String name, User owner) {
        return Project.builder()
                .id(id)
                .name(name)
                .description("Description for " + name)
                .owner(owner)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }

    private Task createTestTask(Long id, String title, Project project, User assignedUser) {
        return Task.builder()
                .id(id)
                .title(title)
                .description("Description for " + title)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(7))
                .project(project)
                .assignedUser(assignedUser)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }

    private TaskDTO createTestTaskDTO(Long id, String title, Long projectId, Long assignedUserId) {
        return TaskDTO.builder()
                .id(id)
                .title(title)
                .description("Description for " + title)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(7))
                .projectId(projectId)
                .assignedUserId(assignedUserId)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }
} 