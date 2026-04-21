package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.dto.user.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = BinaryContentMapper.class)
public interface UserMapper {

    @Mapping(target = "online", expression = "java(user.getStatus() != null && user.getStatus().isOnline())")
    UserDto toDto(User user);
}
