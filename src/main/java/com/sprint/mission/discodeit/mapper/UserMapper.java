package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {BinaryContentMapper.class})
public interface UserMapper {

  @Mapping(target = "id", source = "user.id")
  @Mapping(target = "username", source = "user.username")
  @Mapping(target = "email", source = "user.email")
  @Mapping(target = "profile", source = "user.profile")
  @Mapping(target = "online", source = "isOnline")
  @Mapping(target = "role", source = "user.role")
  UserDto toDto(User user, boolean isOnline);
}