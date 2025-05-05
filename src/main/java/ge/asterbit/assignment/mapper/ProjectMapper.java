package ge.asterbit.assignment.mapper;

import ge.asterbit.assignment.dto.project.CreateProjectRequest;
import ge.asterbit.assignment.dto.project.ProjectDTO;
import ge.asterbit.assignment.dto.project.UpdateProjectRequest;
import ge.asterbit.assignment.entity.Project;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    
    ProjectMapper INSTANCE = Mappers.getMapper(ProjectMapper.class);
    
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerEmail", source = "owner.email")
    ProjectDTO toDTO(Project project);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    Project createRequestToProject(CreateProjectRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    void updateProjectFromRequest(UpdateProjectRequest request, @MappingTarget Project project);
} 