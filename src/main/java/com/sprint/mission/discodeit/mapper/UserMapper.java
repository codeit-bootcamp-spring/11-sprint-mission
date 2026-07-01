package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.userdto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

  private final BinaryContentMapper binaryContentMapper;
  private final SessionRegistry sessionRegistry;

  public UserDto toDto(User user) {

    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getRole(),
        user.getProfile() != null ? binaryContentMapper.toDto(user.getProfile()) : null,
        isOnline(user.getId())

    );

  }

  private boolean isOnline(UUID userId) {
    return sessionRegistry.getAllPrincipals().stream()
        .filter(p -> p instanceof DiscodeitUserDetails details
            && details.getUserDto().id().equals(userId))
        .anyMatch(p -> !sessionRegistry.getAllSessions(p, false).isEmpty());
  }

}
