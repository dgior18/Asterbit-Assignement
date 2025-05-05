package ge.asterbit.assignment.service;

import ge.asterbit.assignment.dto.project.CreateProjectRequest;
import ge.asterbit.assignment.dto.project.ProjectDTO;
import ge.asterbit.assignment.dto.project.UpdateProjectRequest;
import ge.asterbit.assignment.entity.Project;
import ge.asterbit.assignment.entity.Role;
import ge.asterbit.assignment.entity.User;
import ge.asterbit.assignment.exception.AccessDeniedException;
import ge.asterbit.assignment.exception.ResourceNotFoundException;
import ge.asterbit.assignment.mapper.ProjectMapper;
import ge.asterbit.assignment.repository.ProjectRepository;
import ge.asterbit.assignment.service.impl.ProjectServiceImpl;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private UserService userService;

    private ProjectServiceImpl projectService;

    private User adminUser;
    private User managerUser;
    private User regularUser;
    private Project project;
    private ProjectDTO projectDTO;

    @BeforeEach
    void setUp() {
        projectService = new ProjectServiceImpl(projectRepository, projectMapper, userService);

        adminUser = createTestUser(1L, "admin@test.com", Role.ADMIN);
        managerUser = createTestUser(2L, "manager@test.com", Role.MANAGER);
        regularUser = createTestUser(3L, "user@test.com", Role.USER);
        project = createTestProject(1L, "Test Project", managerUser);
        projectDTO = createTestProjectDTO(1L, "Test Project", managerUser.getId());
    }

    @Test
    void getAllProjects_ShouldReturnPageOfProjectDTOs_WhenUserIsAdmin() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> projectPage = new PageImpl<>(List.of(project));

        when(userService.getCurrentUserEntity()).thenReturn(adminUser);
        when(projectRepository.findAll(pageable)).thenReturn(projectPage);
        when(projectMapper.toDTO(any(Project.class))).thenReturn(projectDTO);

        // Act
        Page<ProjectDTO> result = projectService.getAllProjects(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(projectDTO, result.getContent().get(0));

        verify(userService).getCurrentUserEntity();
        verify(projectRepository).findAll(pageable);
        verify(projectMapper).toDTO(project);
    }

    @Test
    void getAllProjects_ShouldThrowException_WhenUserIsNotAdmin() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        when(userService.getCurrentUserEntity()).thenReturn(regularUser);

        // Act & Assert
        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            projectService.getAllProjects(pageable);
        });

        assertEquals("Only administrators can access all projects", exception.getMessage());

        verify(userService).getCurrentUserEntity();
        verify(projectRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getMyProjects_ShouldReturnPageOfOwnerProjects() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> projectPage = new PageImpl<>(List.of(project));

        when(userService.getCurrentUserEntity()).thenReturn(managerUser);
        when(projectRepository.findByOwner(managerUser, pageable)).thenReturn(projectPage);
        when(projectMapper.toDTO(any(Project.class))).thenReturn(projectDTO);

        // Act
        Page<ProjectDTO> result = projectService.getMyProjects(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(projectDTO, result.getContent().get(0));

        verify(userService).getCurrentUserEntity();
        verify(projectRepository).findByOwner(managerUser, pageable);
        verify(projectMapper).toDTO(project);
    }

    @Test
    void getProjectById_ShouldReturnProjectDTO_WhenAdminAccesses() {
        // Arrange
        Long projectId = 1L;

        when(userService.getCurrentUserEntity()).thenReturn(adminUser);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMapper.toDTO(project)).thenReturn(projectDTO);

        // Act
        ProjectDTO result = projectService.getProjectById(projectId);

        // Assert
        assertNotNull(result);
        assertEquals(projectDTO, result);

        verify(userService).getCurrentUserEntity();
        verify(projectRepository).findById(projectId);
        verify(projectMapper).toDTO(project);
    }

    @Test
    void getProjectById_ShouldReturnProjectDTO_WhenOwnerAccesses() {
        // Arrange
        Long projectId = 1L;

        when(userService.getCurrentUserEntity()).thenReturn(managerUser);
        when(projectRepository.findByIdAndOwner(projectId, managerUser)).thenReturn(Optional.of(project));
        when(projectMapper.toDTO(project)).thenReturn(projectDTO);

        // Act
        ProjectDTO result = projectService.getProjectById(projectId);

        // Assert
        assertNotNull(result);
        assertEquals(projectDTO, result);

        verify(userService).getCurrentUserEntity();
        verify(projectRepository).findByIdAndOwner(projectId, managerUser);
        verify(projectMapper).toDTO(project);
    }

    @Test
    void getProjectById_ShouldThrowException_WhenProjectNotFound() {
        // Arrange
        Long projectId = 1L;

        when(userService.getCurrentUserEntity()).thenReturn(managerUser);
        when(projectRepository.findByIdAndOwner(projectId, managerUser)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            projectService.getProjectById(projectId);
        });

        assertTrue(exception.getMessage().contains("Project"));
        assertTrue(exception.getMessage().contains(projectId.toString()));

        verify(userService).getCurrentUserEntity();
        verify(projectRepository).findByIdAndOwner(projectId, managerUser);
        verify(projectMapper, never()).toDTO(any(Project.class));
    }

    @Test
    void createProject_ShouldCreateAndReturnProjectDTO_WhenManagerCreates() {
        // Arrange
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("New Project")
                .description("Project Description")
                .build();

        Project newProject = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Project savedProject = Project.builder()
                .id(2L)
                .name(request.getName())
                .description(request.getDescription())
                .owner(managerUser)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        ProjectDTO savedProjectDTO = ProjectDTO.builder()
                .id(2L)
                .name(request.getName())
                .description(request.getDescription())
                .ownerId(managerUser.getId())
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        when(userService.getCurrentUserEntity()).thenReturn(managerUser);
        when(projectMapper.createRequestToProject(request)).thenReturn(newProject);
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);
        when(projectMapper.toDTO(savedProject)).thenReturn(savedProjectDTO);

        // Act
        ProjectDTO result = projectService.createProject(request);

        // Assert
        assertNotNull(result);
        assertEquals(savedProjectDTO, result);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());
        Project capturedProject = projectCaptor.getValue();
        assertEquals(request.getName(), capturedProject.getName());
        assertEquals(request.getDescription(), capturedProject.getDescription());
        assertEquals(managerUser, capturedProject.getOwner());

        verify(userService).getCurrentUserEntity();
        verify(projectMapper).createRequestToProject(request);
        verify(projectMapper).toDTO(savedProject);
    }

    @Test
    void createProject_ShouldThrowException_WhenRegularUserCreates() {
        // Arrange
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("New Project")
                .description("Project Description")
                .build();

        when(userService.getCurrentUserEntity()).thenReturn(regularUser);

        // Act & Assert
        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            projectService.createProject(request);
        });

        assertEquals("Users cannot create projects", exception.getMessage());

        verify(userService).getCurrentUserEntity();
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void updateProject_ShouldUpdateAndReturnProjectDTO() {
        // Arrange
        Long projectId = 1L;
        UpdateProjectRequest request = UpdateProjectRequest.builder()
                .name("Updated Project")
                .description("Updated Description")
                .build();

        Project updatedProject = Project.builder()
                .id(projectId)
                .name("Test Project")
                .description("Description for Test Project")
                .owner(managerUser)
                .createDate(project.getCreateDate())
                .updateDate(LocalDateTime.now())
                .build();

        ProjectDTO updatedProjectDTO = ProjectDTO.builder()
                .id(projectId)
                .name("Test Project")
                .description("Description for Test Project")
                .ownerId(managerUser.getId())
                .createDate(project.getCreateDate())
                .updateDate(LocalDateTime.now())
                .build();

        when(userService.getCurrentUserEntity()).thenReturn(managerUser);
        when(projectRepository.findByIdAndOwner(projectId, managerUser)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(updatedProject);
        when(projectMapper.toDTO(updatedProject)).thenReturn(updatedProjectDTO);

        // Act
        ProjectDTO result = projectService.updateProject(projectId, request);

        // Assert
        assertNotNull(result);
        assertEquals(updatedProjectDTO, result);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());
        Project capturedProject = projectCaptor.getValue();
        assertEquals("Test Project", capturedProject.getName());
        assertEquals("Description for Test Project", capturedProject.getDescription());

        verify(userService).getCurrentUserEntity();
        verify(projectRepository).findByIdAndOwner(projectId, managerUser);
        verify(projectMapper).toDTO(updatedProject);
    }

    @Test
    void getProjectEntityById_ShouldReturnProject_WhenProjectExists() {
        // Arrange
        Long projectId = 1L;

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // Act
        Project result = projectService.getProjectEntityById(projectId);

        // Assert
        assertNotNull(result);
        assertEquals(project, result);

        verify(projectRepository).findById(projectId);
    }

    @Test
    void getProjectEntityById_ShouldThrowException_WhenProjectDoesNotExist() {
        // Arrange
        Long projectId = 1L;

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            projectService.getProjectEntityById(projectId);
        });

        assertTrue(exception.getMessage().contains("Project"));
        assertTrue(exception.getMessage().contains(projectId.toString()));

        verify(projectRepository).findById(projectId);
    }

    @Test
    void getProjectEntityByIdAndOwner_ShouldReturnProject_WhenProjectExists() {
        // Arrange
        Long projectId = 1L;

        when(projectRepository.findByIdAndOwner(projectId, managerUser)).thenReturn(Optional.of(project));

        // Act
        Project result = projectService.getProjectEntityByIdAndOwner(projectId, managerUser);

        // Assert
        assertNotNull(result);
        assertEquals(project, result);

        verify(projectRepository).findByIdAndOwner(projectId, managerUser);
    }

    @Test
    void getProjectEntityByIdAndOwner_ShouldThrowException_WhenProjectDoesNotExist() {
        // Arrange
        Long projectId = 1L;

        when(projectRepository.findByIdAndOwner(projectId, managerUser)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            projectService.getProjectEntityByIdAndOwner(projectId, managerUser);
        });

        assertTrue(exception.getMessage().contains("Project"));
        assertTrue(exception.getMessage().contains(projectId.toString()));

        verify(projectRepository).findByIdAndOwner(projectId, managerUser);
    }

    @Test
    void deleteProject_ShouldDeleteProject_WhenOwnerDeletes() {
        // Arrange
        Long projectId = 1L;

        when(userService.getCurrentUserEntity()).thenReturn(managerUser);
        when(projectRepository.findByIdAndOwner(projectId, managerUser)).thenReturn(Optional.of(project));

        // Act
        projectService.deleteProject(projectId);

        // Assert
        verify(userService).getCurrentUserEntity();
        verify(projectRepository).findByIdAndOwner(projectId, managerUser);
        verify(projectRepository).delete(project);
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

    private ProjectDTO createTestProjectDTO(Long id, String name, Long ownerId) {
        return ProjectDTO.builder()
                .id(id)
                .name(name)
                .description("Description for " + name)
                .ownerId(ownerId)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }
} 