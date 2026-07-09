package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OnlineStatusResolver {

  private final SessionRegistry sessionRegistry;

  public boolean isOnline(UUID userId) {
    return sessionRegistry.getAllPrincipals().stream()
        .filter(p -> p instanceof DiscodeitUserDetails)
        .map(p -> (DiscodeitUserDetails) p)
        .filter(d -> d.getUserDto().id().equals(userId))
        .anyMatch(d -> !sessionRegistry.getAllSessions(d, false).isEmpty());
  }
}
