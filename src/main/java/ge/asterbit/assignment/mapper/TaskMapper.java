package ge.asterbit.assignment.mapper;

import ge.asterbit.assignment.dto.task.CreateTaskRequest;
import ge.asterbit.assignment.dto.task.TaskDTO;
import ge.asterbit.assignment.dto.task.UpdateTaskRequest;
import ge.asterbit.assignment.entity.Task;
import ge.asterbit.assignment.entity.TaskStatus;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    
    TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);
    
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "assignedUserId", source = "assignedUser.id")
    @Mapping(target = "assignedUserEmail", source = "assignedUser.email")
    TaskDTO toDTO(Task task);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "TODO")
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "assignedUser", ignore = true) 
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    Task createRequestToTask(CreateTaskRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "assignedUser", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    void updateTaskFromRequest(UpdateTaskRequest request, @MappingTarget Task task);
} 