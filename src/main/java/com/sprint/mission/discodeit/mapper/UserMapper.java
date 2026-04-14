package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {BinaryContentMapper.class})
public abstract class UserMapper {

  @Mapping(source = "user.id", target = "id")
  @Mapping(source = "user.username", target = "username")
  @Mapping(source = "user.email", target = "email")
  @Mapping(source = "user.profile", target = "profile")
  public abstract UserDto toDtoBasic(User user);

  public UserDto toDto(User user, UserStatus status) {
    if (user == null) {
      return null;
    }
    UserDto dto = toDtoBasic(user);

    boolean isOnline = Optional.ofNullable(status)
        .map(UserStatus::getOnlineStatus).orElse(false);

    return new UserDto(dto.id(), dto.username(), dto.email(), dto.profile(), isOnline);
  }
}
