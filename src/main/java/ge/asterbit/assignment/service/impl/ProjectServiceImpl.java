package ge.asterbit.assignment.service.impl;

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
import ge.asterbit.assignment.service.ProjectService;
import ge.asterbit.assignment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserService userService;

    @Override
    public Page<ProjectDTO> getAllProjects(Pageable pageable) {
        User currentUser = userService.getCurrentUserEntity();
        
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only administrators can access all projects");
        }
        
        return projectRepository.findAll(pageable)
                .map(projectMapper::toDTO);
    }

    @Override
    public Page<ProjectDTO> getMyProjects(Pageable pageable) {
        User currentUser = userService.getCurrentUserEntity();
        return projectRepository.findByOwner(currentUser, pageable)
                .map(projectMapper::toDTO);
    }

    @Override
    public ProjectDTO getProjectById(Long id) {
        User currentUser = userService.getCurrentUserEntity();
        
        Project project = findProjectAndCheckAccess(id, currentUser);
        return projectMapper.toDTO(project);
    }
    
    @Override
    public Project getProjectEntityById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
    }
    
    @Override
    public Project getProjectEntityByIdAndOwner(Long id, User owner) {
        return projectRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
    }

    @Override
    @Transactional
    public ProjectDTO createProject(CreateProjectRequest request) {
        User currentUser = userService.getCurrentUserEntity();
        
        if (currentUser.getRole() == Role.USER) {
            throw new AccessDeniedException("Users cannot create projects");
        }
        
        Project project = projectMapper.createRequestToProject(request);
        project.setOwner(currentUser);
        
        Project savedProject = projectRepository.save(project);
        return projectMapper.toDTO(savedProject);
    }

    @Override
    @Transactional
    public ProjectDTO updateProject(Long id, UpdateProjectRequest request) {
        User currentUser = userService.getCurrentUserEntity();
        
        Project project = findProjectAndCheckAccess(id, currentUser);
        projectMapper.updateProjectFromRequest(request, project);
        
        Project updatedProject = projectRepository.save(project);
        return projectMapper.toDTO(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        User currentUser = userService.getCurrentUserEntity();
        
        Project project = findProjectAndCheckAccess(id, currentUser);
        projectRepository.delete(project);
    }
    
    private Project findProjectAndCheckAccess(Long id, User user) {
        Project project;
        
        if (user.getRole() == Role.ADMIN) {
            project = projectRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        } else {
            project = projectRepository.findByIdAndOwner(id, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        }
        
        return project;
    }
} 