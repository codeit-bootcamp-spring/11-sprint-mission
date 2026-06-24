package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;

@Mapper(componentModel = "spring", uses = {BinaryContentMapper.class})
public abstract class UserMapper {

  @Autowired
  private SessionRegistry sessionRegistry;

  @Mapping(source = "user.id", target = "id")
  @Mapping(source = "user.username", target = "username")
  @Mapping(source = "user.email", target = "email")
  @Mapping(source = "user.profile", target = "profile")
  @Mapping(source = "user.role", target = "role")
  public abstract UserDto toDtoBasic(User user);

  public UserDto toDto(User user) {
    if (user == null) {
      return null;
    }
    UserDto dto = toDtoBasic(user);

    boolean isOnline = sessionRegistry.getAllPrincipals().stream()
        .filter(p -> p instanceof DiscodeitUserDetails)
        .map(p -> (DiscodeitUserDetails) p)
        .anyMatch(p -> p.getUserDto().id().equals(user.getId()));

    return new UserDto(dto.id(), dto.username(), dto.email(), dto.profile(), isOnline,
        dto.role());
  }
}
