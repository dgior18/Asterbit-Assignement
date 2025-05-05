package ge.asterbit.assignment.service;

import ge.asterbit.assignment.dto.project.CreateProjectRequest;
import ge.asterbit.assignment.dto.project.ProjectDTO;
import ge.asterbit.assignment.dto.project.UpdateProjectRequest;
import ge.asterbit.assignment.entity.Project;
import ge.asterbit.assignment.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
    Page<ProjectDTO> getAllProjects(Pageable pageable);
    Page<ProjectDTO> getMyProjects(Pageable pageable);
    ProjectDTO getProjectById(Long id);
    ProjectDTO createProject(CreateProjectRequest request);
    ProjectDTO updateProject(Long id, UpdateProjectRequest request);
    void deleteProject(Long id);
    
    Project getProjectEntityById(Long id);
    Project getProjectEntityByIdAndOwner(Long id, User owner);
} 