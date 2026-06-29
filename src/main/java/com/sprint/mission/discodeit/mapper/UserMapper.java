package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {BinaryContentMapper.class})
public abstract class UserMapper {

  @Mapping(source = "user.id", target = "id")
  @Mapping(source = "user.username", target = "username")
  @Mapping(source = "user.email", target = "email")
  @Mapping(source = "user.profile", target = "profile")
  @Mapping(source = "user.role", target = "role")
  public abstract UserDto toDtoBasic(User user);

  public UserDto toDto(User user, boolean isOnline) {
    if (user == null) {
      return null;
    }
    UserDto dto = toDtoBasic(user);

    return new UserDto(dto.id(), dto.username(), dto.email(), dto.profile(), isOnline,
        dto.role());
  }
}
