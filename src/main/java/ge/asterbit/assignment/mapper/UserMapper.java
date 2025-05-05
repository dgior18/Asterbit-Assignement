package ge.asterbit.assignment.mapper;

import ge.asterbit.assignment.dto.auth.RegisterRequest;
import ge.asterbit.assignment.dto.user.UserDTO;
import ge.asterbit.assignment.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    
    UserDTO toDTO(User user);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "password", source = "password")
    @Mapping(target = "role", ignore = true)
    User registerRequestToUser(RegisterRequest registerRequest);
} 